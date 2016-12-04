package org.deeplearning4j.nn.layers.variational;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.variational.ReconstructionDistribution;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.params.VariationalAutoencoderParamInitializer;
import org.deeplearning4j.optimize.Solver;
import org.deeplearning4j.optimize.api.ConvexOptimizer;
import org.deeplearning4j.optimize.api.IterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.*;

import static org.deeplearning4j.nn.params.VariationalAutoencoderParamInitializer.BIAS_KEY_SUFFIX;
import static org.deeplearning4j.nn.params.VariationalAutoencoderParamInitializer.WEIGHT_KEY_SUFFIX;

/**
 * Created by Alex on 25/11/2016.
 */
@Data
public class VariationalAutoencoder implements Layer {

    protected INDArray input;
    protected INDArray paramsFlattened;
    protected INDArray gradientsFlattened;
    protected Map<String,INDArray> params;
    protected transient Map<String,INDArray> gradientViews;
    protected NeuralNetConfiguration conf;
    protected INDArray dropoutMask;
    protected boolean dropoutApplied = false;
    protected double score = 0.0;
    protected ConvexOptimizer optimizer;
    protected Gradient gradient;
    protected Collection<IterationListener> iterationListeners = new ArrayList<>();
    protected int index = 0;
    protected INDArray maskArray;
    protected Solver solver;

    protected int[] encoderLayerSizes;
    protected int[] decoderLayerSizes;
    protected ReconstructionDistribution reconstructionDistribution;
    protected String pzxAfn;

    public VariationalAutoencoder(NeuralNetConfiguration conf){
        this.conf = conf;

        this.encoderLayerSizes = ((org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder)conf.getLayer()).getEncoderLayerSizes();
        this.decoderLayerSizes = ((org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder)conf.getLayer()).getDecoderLayerSizes();
        this.reconstructionDistribution = ((org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder)conf.getLayer()).getOutputDistribution();
        this.pzxAfn = ((org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder)conf.getLayer()).getPzxActivationFunction();
    }


    @Override
    public void update(Gradient gradient) {

    }

    @Override
    public void update(INDArray gradient, String paramType) {

    }

    @Override
    public double score() {
        return score;
    }

    @Override
    public void computeGradientAndScore() {
        //First: do the full forward pass, through the network (including the random sampling, etc)
        //TODO handle multiple samples as an option...
        VAEFwdHelper fwd = doForward(true, true);

        INDArray pzxLogStdev2W = params.get(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_W);
        INDArray pzxLogStdev2b = params.get(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_B);

        INDArray pzxLogStdev2Pre = fwd.encoderActivations[fwd.encoderActivations.length-1].mmul(pzxLogStdev2W).addiRowVector(pzxLogStdev2b);

        INDArray mu = fwd.pzxMeanPreOut.dup();
        INDArray pzxLogStdev2 = pzxLogStdev2Pre.dup();
        if(!"identity".equals(pzxAfn)){
            Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                    pzxAfn, mu, conf.getExtraArgs() ));
            Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                    pzxAfn, pzxLogStdev2, conf.getExtraArgs() ));
        }


        INDArray pzxSigma = Transforms.exp(pzxLogStdev2,true);
        Transforms.sqrt(pzxSigma,false);

        int minibatch = input.size(0);
        int size = fwd.pzxMeanPreOut.size(1);

        INDArray e = Nd4j.rand(minibatch, size);
        INDArray z = mu.add(pzxSigma.mul(e));      //z = mu + sigma * e, with e ~ N(0,1)

        //Next: need to do forward pass through decoder...

        int nDecoderLayers = decoderLayerSizes.length;
        INDArray current = z;
        INDArray[] decoderPreOut = new INDArray[nDecoderLayers];
        INDArray[] decoderActivations = new INDArray[nDecoderLayers];
        for( int i=0; i<nDecoderLayers; i++ ){
            String wKey = "d" + i + WEIGHT_KEY_SUFFIX;
            String bKey = "d" + i + BIAS_KEY_SUFFIX;

            INDArray weights = params.get(wKey);
            INDArray bias = params.get(bKey);

            current = current.mmul(weights).addiRowVector(bias);
            decoderPreOut[i] = current.dup();
            Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                    conf.getLayer().getActivationFunction(), current, conf.getExtraArgs() ));
            decoderActivations[i] = current;
        }

        INDArray xzw = params.get(VariationalAutoencoderParamInitializer.PXZ_W);
        INDArray xzb = params.get(VariationalAutoencoderParamInitializer.PXZ_B);


        INDArray pxzDistributionParams = current.mmul(xzw).addiRowVector(xzb);
        this.score = reconstructionDistribution.logProbability(input, pxzDistributionParams, true);
        //Need to add other component of score:
        INDArray temp = mu.mul(mu).addi(pzxSigma.mul(pzxSigma)).negi();
        temp.addi(pzxLogStdev2).addi(1.0);
        double scorePt1 = 0.5 / minibatch * temp.sumNumber().doubleValue();
        this.score += scorePt1;
        this.score += (calcL1(false) + calcL2(false))/minibatch;

        INDArray dpdpxz = reconstructionDistribution.gradient(input, pxzDistributionParams);


        /////////////////////////////////////////////////////////
        Gradient gradient = new DefaultGradient(gradientsFlattened);

        INDArray dLdxzw = gradientViews.get(VariationalAutoencoderParamInitializer.PXZ_W);
        INDArray dLdxzb = gradientViews.get(VariationalAutoencoderParamInitializer.PXZ_B);
        INDArray lastDecActivations = decoderActivations[decoderActivations.length-1];
        Nd4j.gemm(lastDecActivations,dpdpxz,dLdxzw,true,false,1.0,0.0);
        dLdxzb.assign(dpdpxz.sum(0));    //TODO: do this without the assign

        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PXZ_W, dLdxzw); //TODO not sure on order here...
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PXZ_B, dLdxzb);

        //Do backprop for output probability distribution -> final decoder layer
        INDArray epsilon = xzw.mmul(dpdpxz.transpose()).transpose();

        //Next: we chain derivatives backwards...
        String afn = conf().getLayer().getActivationFunction();


        for( int i=nDecoderLayers-1; i>=0; i-- ){
            String wKey = "d" + i + WEIGHT_KEY_SUFFIX;
            String bKey = "d" + i + BIAS_KEY_SUFFIX;

            INDArray sigmaPrimeZ = Nd4j.getExecutioner().execAndReturn(
                Nd4j.getOpFactory().createTransform(afn, decoderPreOut[i]).derivative());

            INDArray currentDelta = epsilon.muli(sigmaPrimeZ);

            INDArray weights = params.get(wKey);
            INDArray dLdW = gradientViews.get(wKey);
            INDArray dLdB = gradientViews.get(bKey);

            INDArray actInput;
            if (i == 0) {
                actInput = z;
            } else {
                actInput = decoderActivations[i-1];
            }

            Nd4j.gemm(actInput,currentDelta,dLdW,true,false,1.0,0.0);
            dLdB.assign(currentDelta.sum(0));    //TODO: do this without the assign

            gradient.gradientForVariable().put(wKey, dLdW);
            gradient.gradientForVariable().put(bKey, dLdB);

            epsilon = weights.mmul(currentDelta.transpose()).transpose();
        }

        //Backprop through p(z|x)
        INDArray eZXMeanW = params.get(VariationalAutoencoderParamInitializer.PZX_MEAN_W);
        INDArray eZXLogStdev2W = params.get(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_W);

        INDArray dLdz = epsilon;
        INDArray dLdmu = dLdz.sub(mu);

        INDArray dLdLogSigma2 = dLdz.mul(e).muli(pzxSigma)
                .subi(pzxSigma.mul(pzxSigma)).addi(1).muli(0.5);

        INDArray dLdZXMeanW = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_MEAN_W);
        INDArray dLdZXLogStdev2W = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_W);
        INDArray dLdZXMeanb = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_MEAN_B);
        INDArray dLdZXLogStdev2b = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_B);


        INDArray dLdPreMu = dLdmu.mul(Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                pzxAfn, fwd.getPzxMeanPreOut().dup(), conf.getExtraArgs() ).derivative()));

        INDArray dLdPreLogSigma2 = dLdLogSigma2.mul(Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                pzxAfn, pzxLogStdev2Pre.dup(), conf.getExtraArgs() ).derivative()));

        INDArray lastEncoderActivation = fwd.encoderActivations[fwd.encoderActivations.length-1];
        Nd4j.gemm(lastEncoderActivation, dLdPreMu, dLdZXMeanW, true, false, 1.0, 0.0);
        Nd4j.gemm(lastEncoderActivation, dLdPreLogSigma2, dLdZXLogStdev2W, true, false, 1.0, 0.0);

        INDArray sigmaPrimePreMu = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                pzxAfn, fwd.getPzxMeanPreOut().dup(), conf.getExtraArgs() ).derivative());
        dLdZXMeanb.assign(dLdz.sub(mu).mul(sigmaPrimePreMu).sum(0));

        dLdZXLogStdev2b.assign(dLdPreLogSigma2.sum(0));

        //TODO check order
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_MEAN_W, dLdZXMeanW);
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_MEAN_B, dLdZXMeanb);
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_W, dLdZXLogStdev2W);
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_LOGSTD2_B, dLdZXLogStdev2b);

        epsilon = eZXMeanW.mmul(dLdPreMu.transpose()).transpose();
        epsilon.addi(eZXLogStdev2W.mmul(dLdPreLogSigma2.transpose()).transpose());


        //Backprop through encoder:
        //TODO code reuse with non-pretrain backprop
        int nEncoderLayers = encoderLayerSizes.length;
        for( int i=nEncoderLayers-1; i>=0; i-- ){
            String wKey = "e" + i + WEIGHT_KEY_SUFFIX;
            String bKey = "e" + i + BIAS_KEY_SUFFIX;

            INDArray weights = params.get(wKey);

            INDArray dLdW = gradientViews.get(wKey);
            INDArray dLdB = gradientViews.get(bKey);

            INDArray preOut = fwd.encoderPreOuts[i];
            INDArray activationDerivative = Nd4j.getExecutioner().execAndReturn(
                    Nd4j.getOpFactory().createTransform(afn, preOut).derivative());

            INDArray currentDelta = epsilon.muli(activationDerivative);

            INDArray actInput;
            if(i == 0){
                actInput = input;
            } else {
                actInput = fwd.encoderActivations[i-1];
            }
            Nd4j.gemm(actInput,currentDelta,dLdW,true,false,1.0,0.0);
            dLdB.assign(currentDelta.sum(0));    //TODO: do this without the assign

            gradient.gradientForVariable().put(wKey, dLdW);
            gradient.gradientForVariable().put(bKey, dLdB);

            epsilon = weights.mmul(currentDelta.transpose()).transpose();
        }

        this.gradient = gradient;
    }

    @Override
    public void accumulateScore(double accum) {

    }

    @Override
    public INDArray params() {
        return paramsFlattened;
    }

    @Override
    public int numParams() {
        return numParams(false);
    }

    @Override
    public int numParams(boolean backwards) {
        int ret = 0;
        for(Map.Entry<String,INDArray> entry : params.entrySet()){
            if(backwards && (entry.getKey().startsWith("d") || entry.getKey().startsWith("eZXLogStdev2"))) continue;
            ret += entry.getValue().length();
        }
        return ret;
    }

    @Override
    public void setParams(INDArray params) {
        if(params.length() != this.paramsFlattened.length()){
            throw new IllegalArgumentException("Cannot set parameters: expected parameters vector of length " +
                this.paramsFlattened.length() + " but got parameters array of length " + params.length());
        }
        this.paramsFlattened.assign(params);
    }

    @Override
    public void setParamsViewArray(INDArray params) {
        this.paramsFlattened = params;
        //TODO flattening/unflattening...
    }

    @Override
    public void setBackpropGradientsViewArray(INDArray gradients) {
        if(this.params != null && gradients.length() != numParams()){
            throw new IllegalArgumentException("Invalid input: expect gradients array of length " + numParams()
                    + ", got gradient array of length of length " + gradients.length());
        }

        this.gradientsFlattened = gradients;
        this.gradientViews = conf.getLayer().initializer().getGradientsFromFlattened(conf,gradients);
    }

    @Override
    public void applyLearningRateScoreDecay() {

    }

    @Override
    public void fit(INDArray data) {
        this.setInput(data);
        fit();
    }

    @Override
    public void iterate(INDArray input) {
        fit(input);
    }

    @Override
    public Gradient gradient() {
        return gradient;
    }

    @Override
    public Pair<Gradient, Double> gradientAndScore() {
        return new Pair<>(gradient(), score());
    }

    @Override
    public int batchSize() {
        return input.size(0);
    }

    @Override
    public NeuralNetConfiguration conf() {
        return conf;
    }

    @Override
    public void setConf(NeuralNetConfiguration conf) {
        this.conf = conf;
    }

    @Override
    public INDArray input() {
        return input;
    }

    @Override
    public void validateInput() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ConvexOptimizer getOptimizer() {
        return optimizer;
    }

    @Override
    public INDArray getParam(String param) {
        return params.get(param);
    }

    @Override
    public void initParams() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Map<String, INDArray> paramTable() {
        return new LinkedHashMap<>(params);
    }

    @Override
    public void setParamTable(Map<String, INDArray> paramTable) {
        this.params = paramTable;
    }

    @Override
    public void setParam(String key, INDArray val) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void clear() {
        this.input = null;
        this.maskArray = null;
    }

    private boolean isPretrainParam(String param){
        return !(param.startsWith("e") || param.startsWith("pZXMean"));
    }

    @Override
    public double calcL2(boolean backpropParamsOnly) {
        if(!conf.isUseRegularization() || conf.getLayer().getL2() <= 0.0 ) return 0.0;

        double l2Sum = 0.0;
        for(Map.Entry<String,INDArray> e : paramTable().entrySet()){
            double l2 = conf().getL2ByParam(e.getKey());
            if(l2 <= 0.0 || (backpropParamsOnly && isPretrainParam(e.getKey()))){
                continue;
            }

            double l2Norm = e.getValue().norm2Number().doubleValue();
            l2Sum += 0.5 * l2 * l2Norm * l2Norm;
        }

        return l2Sum;
    }

    @Override
    public double calcL1(boolean backpropParamsOnly) {
        if(!conf.isUseRegularization() || conf.getLayer().getL1() <= 0.0 ) return 0.0;

        double l1Sum = 0.0;
        for(Map.Entry<String,INDArray> e : paramTable().entrySet()){
            double l1 = conf().getL1ByParam(e.getKey());
            if(l1 <= 0.0 || (backpropParamsOnly && isPretrainParam(e.getKey()))){
                continue;
            }

            l1Sum += l1 * e.getValue().norm1Number().doubleValue();
        }
        return l1Sum;
    }

    @Override
    public Type type() {
        return Type.FEED_FORWARD;
    }

    @Override
    public Gradient error(INDArray input) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public INDArray derivativeActivation(INDArray input) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Gradient calcGradient(Gradient layerError, INDArray indArray) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Pair<Gradient, INDArray> backpropGradient(INDArray epsilon) {
        Gradient gradient = new DefaultGradient();

        VAEFwdHelper fwd = doForward(true, true);
        INDArray activationDerivative = Nd4j.getExecutioner().execAndReturn(
                Nd4j.getOpFactory().createTransform(pzxAfn, fwd.pzxMeanPreOut).derivative());

        INDArray currentDelta = epsilon.muli(activationDerivative);

        //Finally, calculate mean value:
        INDArray meanW = params.get(VariationalAutoencoderParamInitializer.PZX_MEAN_W);
        INDArray dLdMeanW = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_MEAN_W); //f order
        INDArray lastEncoderActivation = fwd.encoderActivations[fwd.encoderActivations.length-1];
        Nd4j.gemm(lastEncoderActivation,currentDelta,dLdMeanW,true,false,1.0,0.0);
        INDArray dLdMeanB = gradientViews.get(VariationalAutoencoderParamInitializer.PZX_MEAN_B);
        dLdMeanB.assign(currentDelta.sum(0));    //TODO: do this without the assign

        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_MEAN_W, dLdMeanW);
        gradient.gradientForVariable().put(VariationalAutoencoderParamInitializer.PZX_MEAN_B, dLdMeanB);

        epsilon = meanW.mmul(currentDelta.transpose()).transpose();

        int nEncoderLayers = encoderLayerSizes.length;

        String afn = conf().getLayer().getActivationFunction();
        for( int i=nEncoderLayers-1; i>=0; i-- ){
            String wKey = "e" + i + WEIGHT_KEY_SUFFIX;
            String bKey = "e" + i + BIAS_KEY_SUFFIX;

            INDArray weights = params.get(wKey);

            INDArray dLdW = gradientViews.get(wKey);
            INDArray dLdB = gradientViews.get(bKey);

            INDArray preOut = fwd.encoderPreOuts[i];
            activationDerivative = Nd4j.getExecutioner().execAndReturn(
                    Nd4j.getOpFactory().createTransform(afn, preOut).derivative());

            currentDelta = epsilon.muli(activationDerivative);

            INDArray actInput;
            if(i == 0){
                actInput = input;
            } else {
                actInput = fwd.encoderActivations[i-1];
            }
            Nd4j.gemm(actInput,currentDelta,dLdW,true,false,1.0,0.0);
            dLdB.assign(currentDelta.sum(0));    //TODO: do this without the assign

            gradient.gradientForVariable().put(wKey, dLdW);
            gradient.gradientForVariable().put(bKey, dLdB);

            epsilon = weights.mmul(currentDelta.transpose()).transpose();
        }

        return new Pair<>(gradient, epsilon);
    }

    @Override
    public void merge(Layer layer, int batchSize) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public INDArray activationMean() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public INDArray preOutput(INDArray x) {
        return preOutput(x, TrainingMode.TEST);
    }

    @Override
    public INDArray preOutput(INDArray x, TrainingMode training) {
        return preOutput(x, training == TrainingMode.TRAIN);
    }

    @Override
    public INDArray preOutput(INDArray x, boolean training) {
        setInput(x);
        return preOutput(training);
    }

    public INDArray preOutput(boolean training) {
        VAEFwdHelper f = doForward(training, false);
        return f.pzxMeanPreOut;
    }

    @AllArgsConstructor @Data
    private static class VAEFwdHelper {
        private INDArray[] encoderPreOuts;
        private INDArray pzxMeanPreOut;
        private INDArray[] encoderActivations;
    }


    private VAEFwdHelper doForward(boolean training, boolean forBackprop){
        if(input == null){
            throw new IllegalStateException("Cannot do forward pass with null input");
        }

        //TODO input validation

        int nEncoderLayers = encoderLayerSizes.length;

        INDArray[] encoderPreOuts = new INDArray[encoderLayerSizes.length];
        INDArray[] encoderActivations = new INDArray[encoderLayerSizes.length];
        INDArray current = input;
        for( int i=0; i<nEncoderLayers; i++ ){
            String wKey = "e" + i + WEIGHT_KEY_SUFFIX;
            String bKey = "e" + i + BIAS_KEY_SUFFIX;

            INDArray weights = params.get(wKey);
            INDArray bias = params.get(bKey);

            current = current.mmul(weights).addiRowVector(bias);
            if(forBackprop){
                encoderPreOuts[i] = current.dup();
            }
            Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                    conf.getLayer().getActivationFunction(), current, conf.getExtraArgs() ));
            encoderActivations[i] = current;
        }

        //Finally, calculate mean value:
        INDArray mW = params.get(VariationalAutoencoderParamInitializer.PZX_MEAN_W);
        INDArray mB = params.get(VariationalAutoencoderParamInitializer.PZX_MEAN_B);

        INDArray pzxMean = current.mmul(mW).addiRowVector(mB);


        return new VAEFwdHelper(encoderPreOuts, pzxMean, encoderActivations);
    }

    @Override
    public INDArray activate(TrainingMode training) {
        return activate(training == TrainingMode.TRAIN);
    }

    @Override
    public INDArray activate(INDArray input, TrainingMode training) {
        return null;
    }

    @Override
    public INDArray activate(boolean training) {
        INDArray output = preOutput(training);  //Mean values for p(z|x)

        if(!"identity".equals(pzxAfn)){
            Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(
                    conf.getLayer().getActivationFunction(), output, conf.getExtraArgs() ));
        }
        return output;
    }

    @Override
    public INDArray activate(INDArray input, boolean training) {
        setInput(input);
        return activate(training);
    }

    @Override
    public INDArray activate() {
        return activate(false);
    }

    @Override
    public INDArray activate(INDArray input) {
        setInput(input);
        return activate();
    }

    @Override
    public Layer transpose() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Layer clone() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Collection<IterationListener> getListeners() {
        if(iterationListeners == null) return null;
        return new ArrayList<>(iterationListeners);
    }

    @Override
    public void setListeners(IterationListener... listeners) {
        setListeners(Arrays.<IterationListener>asList(listeners));
    }

    @Override
    public void setListeners(Collection<IterationListener> listeners) {
        if(iterationListeners == null) iterationListeners = new ArrayList<>();
        else iterationListeners.clear();

        iterationListeners.addAll(listeners);
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setInput(INDArray input) {
        this.input = input;
    }

    @Override
    public void setInputMiniBatchSize(int size) {

    }

    @Override
    public int getInputMiniBatchSize() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setMaskArray(INDArray maskArray) {
        this.maskArray = maskArray;
    }

    @Override
    public INDArray getMaskArray() {
        return maskArray;
    }


    @Override
    public void fit() {
        if(input == null){
            throw new IllegalStateException("Cannot fit layer: layer input is null (not set)");
        }

        if(solver == null){
            solver = new Solver.Builder()
                    .model(this).configure(conf()).listeners(getListeners())
                    .build();
            //Set the updater state view array. For MLN and CG, this is done by MultiLayerUpdater and ComputationGraphUpdater respectively
            Updater updater = solver.getOptimizer().getUpdater();
            int updaterStateSize = updater.stateSizeForLayer(this);
            if(updaterStateSize > 0) updater.setStateViewArray(this, Nd4j.createUninitialized(new int[]{1,updaterStateSize},Nd4j.order()), true);
        }
        this.optimizer = solver.getOptimizer();
        solver.optimize();
    }
}
