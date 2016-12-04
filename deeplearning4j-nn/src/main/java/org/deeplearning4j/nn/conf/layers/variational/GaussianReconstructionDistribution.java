package org.deeplearning4j.nn.conf.layers.variational;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.ops.transforms.Transforms;

/**
 * Created by Alex on 25/11/2016.
 */
public class GaussianReconstructionDistribution implements ReconstructionDistribution {

    private static final double NEG_HALF_LOG_2PI = -0.5*Math.log(2*Math.PI);

    private String activationFn;

    public GaussianReconstructionDistribution(String activationFn){
        this.activationFn = activationFn;
    }

    @Override
    public int distributionInputSize(int dataSize) {
        return 2*dataSize;
    }

    @Override
    public double logProbability(INDArray x, INDArray preOutDistributionParams, boolean average) {
        INDArray output = preOutDistributionParams.dup();
        if(!"identity".equals(activationFn)){
            output = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(activationFn, output));
        }

        int size = output.size(1)/2;
        INDArray mean = output.get(NDArrayIndex.all(), NDArrayIndex.interval(0,size));
        INDArray logStdevSquared = output.get(NDArrayIndex.all(), NDArrayIndex.interval(size,2*size));

        INDArray sigmaSquared = Transforms.exp(logStdevSquared,true);
        INDArray lastTerm = x.sub(mean);
        lastTerm.muli(lastTerm);
        lastTerm.divi(sigmaSquared).divi(2);

        double logProb = x.size(0) * size * NEG_HALF_LOG_2PI - 0.5 * logStdevSquared.sumNumber().doubleValue() - lastTerm.sumNumber().doubleValue();

        if(average){
            return logProb / x.size(0);
        } else {
            return logProb;
        }
    }

    @Override
    public INDArray gradient(INDArray x, INDArray preOutDistributionParams) {
        INDArray output = preOutDistributionParams.dup();
        if(!"identity".equals(activationFn)){
            output = Nd4j.getExecutioner().execAndReturn(Nd4j.getOpFactory().createTransform(activationFn, output));
        }

        int size = output.size(1)/2;
        INDArray mean = output.get(NDArrayIndex.all(), NDArrayIndex.interval(0,size));
        INDArray logStdevSquared = output.get(NDArrayIndex.all(), NDArrayIndex.interval(size,2*size));

        INDArray sigmaSquared = Transforms.exp(logStdevSquared,true);

        INDArray xSubMean = x.sub(mean);
        INDArray xSubMeanSq = xSubMean.mul(xSubMean);

        INDArray dLdmu = xSubMean.divi(sigmaSquared);

        INDArray sigma = Transforms.sqrt(sigmaSquared,true);
        INDArray sigma3 = Transforms.pow(sigmaSquared, 3.0/2);

        INDArray dLdsigma = sigma.rdiv(-1).addi(xSubMeanSq.divi(sigma3));
        INDArray dLdlogSigma2 = sigma.divi(2).muli(dLdsigma);

        INDArray grad = Nd4j.createUninitialized(output.shape());
        grad.put(new INDArrayIndex[]{NDArrayIndex.all(), NDArrayIndex.interval(0,size)}, dLdmu);
        grad.put(new INDArrayIndex[]{NDArrayIndex.all(), NDArrayIndex.interval(size, 2*size)}, dLdlogSigma2);

        if(!"identity".equals(activationFn)){
            INDArray sigmaPrimeZ = Nd4j.getExecutioner().execAndReturn(
                    Nd4j.getOpFactory().createTransform(activationFn, preOutDistributionParams.dup()).derivative());
            grad.muli(sigmaPrimeZ);
        }

        return grad;
    }

    @Override
    public String toString(){
        return "GaussianReconstructionDistribution(afn=" + activationFn + ")";
    }
}
