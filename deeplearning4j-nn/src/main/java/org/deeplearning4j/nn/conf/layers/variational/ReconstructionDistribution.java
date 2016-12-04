package org.deeplearning4j.nn.conf.layers.variational;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by Alex on 25/11/2016.
 */
public interface ReconstructionDistribution {

    int distributionInputSize(int dataSize);

    double logProbability(INDArray x, INDArray preOutDistributionParams, boolean average);

    INDArray gradient(INDArray x, INDArray preOutDistributionParams);

}
