---
title: 
layout: default
---

# IRIS Classifed With a Deep-Belief Net

Deep-belief networks can serve as multinomial classifiers. Given many inputs belonging to various categories, a DBN can learn from a small training set and then classify unlabeled data according to those classes. Given an input record, the DBN will choose one label from a set. 

This goes beyond a Boolean ‘yes’ or ‘no’ to handle a broader, multinomial taxonomy of inputs, where the label chosen is represented as a 1, and all other possible labels are 0s. The network outputs a vector containing one number per output node. The numbers in the vector equals the number of labels to choose from. 

*(To run the Iris example, [use this file](https://github.com/deeplearning4j/dl4j-0.0.3.3-examples/blob/master/src/main/java/org/deeplearning4j/deepbelief/DBNIrisExample.java) and explore others from our [Quick Start page](../quickstart.html).)*

### The IRIS Dataset

The [Iris flower dataset](https://archive.ics.uci.edu/ml/datasets/Iris) is widely used in machine learning to test classification techniques. We will use it to verify the effectiveness of a deep-belief net.

The dataset consists of four measurements taken from 50 examples of each of three species of Iris, so 150 flowers and 600 data points in all (a small dataset...). The various iris species have petals and sepals (the green, leaflike sheaths at the base of petals) of different lengths, measured in centimeters. The length and width of both sepals and petals were taken for the species *Iris setosa, Iris virginica* and *Iris versicolor*. Each species name is a label. 

The continuous nature of those measurements make the Iris dataset a useful dataset for continuous deep-belief networks. Those four features alone can suffice to classify the three species accurately. Success here consists of teaching a neural net to classify by species the data records of individual flowers while knowing only their dimensions, and failure to do the same is a very strong signal that your neural net needs more tuning. 

The dataset is small, which can present its own problems, and the species I. virginica and I. versicolor are so similar that they partially overlap...

Here is a single record:

![data record table](../img/data_record.png)

While the table above is human readable, Deeplearning4j’s algorithms need it to be something more like

     5.1,3.5,1.4,0.2,i.setosa

In fact, let’s take it one step further and get rid of the words, arranging numerical data in two objects:

Data:  5.1,3.5,1.4,0.2    
Label: 0,1,0

Given three output nodes making binary decisions, we can label the three iris species as 1,0,0, or 0,1,0, or 0,0,1. 

### Loading the data

The example will load the data automatically, but this is a good place to talk about how that happens. The IRIS dataset, like many others, comes as a CSV (comma-separated value) file. We use a [general machine-learning vectorization lib called Canova](http://deeplearning4j.org/canova.html) to parse it. 

        log.info("Load data....");
        DataSetIterator iter = new IrisDataSetIterator(batchSize, numSamples);
        DataSet next = iter.next();
        next.normalizeZeroMeanZeroUnitVariance();

Like all Iterators, this one traverses the elements of a list. It makes a pass through the list, accesses each item sequentially, keeps track of how far it has progressed by pointing to its current element, and modifies itself to point to the next element with each new step in the traversal. 

With DL4J, we iterate through input data (a CSV, say) with a DataSetIterator, fetching one or more new examples with each iteration, and loading those examples into a DataSet object that neural nets can work with. Calling *next* on the iterator advances it one step, and returns the new value it points to. 

(The fundamental data structures that DL4J uses for numeric computation are NDArrays. N-dimensional arrays are scalable, multi-dimensional arrays suitable for sophisticated mathematical operations and frequently used in [scientific computing](http://nd4j.org).) 

The DataSetIterator takes two parameters: batchSize specifies how many examples it should fetch with each step, and numSamples specifies the total number of the input data examples to be traversed. 

We then create a DataSet instance called `next` that we preprocess, setting the mean to zero (by subtracting the mean from each element) and the standard deviation to one (dividing by the standard deviation). Normalization maps the original input to another scale, and is also called "scaling." Some objective functions will not work without this so-called [feature scaling](https://en.wikipedia.org/wiki/Feature_scaling), and gradient descent converges (reaches minimal error) much faster when input data is normalized. Given how computationally intensive deep learning is, all efforts should be made to speed training.

### Training and Test Sets

        log.info("Split data....");
        SplitTestAndTrain testAndTrain = next.splitTestAndTrain(splitTrainNum, new Random(seed));
        DataSet train = testAndTrain.getTrain();
        DataSet test = testAndTrain.getTest();

The goal of training a neural net to learn the structure of data, and predict label y given input x, is to produce a classifier that generalizes well; i.e. that can make accurate classifications about data that it has not seen before. 

In other words, learning algorithms accept labeled input data to train on, and result in a classifier. To test whether a classifier generalizes well, you must have something to contrast its guesses against. Therefore, we take the input data, and split it into a training set and a test set. A neural learns on the training set, and only at the end does it check its guessed labels against the ground truth of the test set. For this reason, the algorithm must not be explosed too frequently to the test set. Generalizability can only be established by measuring the performance of a net on data that it has not been exposed to. Tuning a net to perform well on the test set defeats its entire purpose.

### Creating a Neural Network (NN)

To create a neural network, we'll declare the variables and then feed them into the configuration as parameters:

        final int numRows = 4;
        final int numColumns = 1;
        int outputNum = 3;
        int numSamples = 150;
        int batchSize = 150;
        int iterations = 1000;
        int splitTrainNum = (int) (batchSize * .8);
        int seed = 123;
        int listenerFreq = iterations/5;

Here's the configuration in full -- we'll step through it line by line below:

     log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .layer(new RBM()) // NN layer type
            .nIn(numRows * numColumns) // # input nodes
            .nOut(outputNum) // # output nodes
            .seed(seed) 
            .visibleUnit(RBM.VisibleUnit.GAUSSIAN) 
            .hiddenUnit(RBM.HiddenUnit.RECTIFIED) 
            .iterations(iterations) 
            .weightInit(WeightInit.XAVIER) 
            .activationFunction("relu") 
            .l2(2e-4).regularization(true).momentum(0.9).constrainGradientToUnitNorm(true)
            .k(1) // # contrastive divergence iterations
            .lossFunction(LossFunctions.LossFunction.RMSE_XENT) 
            .learningRate(1e-3f) // Backprop step size
            .optimizationAlgo(OptimizationAlgorithm.LBFGS) 
            .list(2) // # NN layers (does not count input layer)
            .hiddenLayerSizes(3) 
            .override(1, new ConfOverride() {
                @Override
                public void overrideLayer(int i, NeuralNetConfiguration.Builder builder) {
                    builder.activationFunction("softmax");
                    builder.layer(new OutputLayer());
                    builder.lossFunction(LossFunctions.LossFunction.MCXENT);
                }
            }).useDropConnect(true)
            .build();

There's a lot to discuss here. The entire configuration is united in one snippet above, and now we'll go through it one parameter at a time:

		log.info("Build model....");

**Line 1** is just a public service announcement for programmers, the first of several. 

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()

**Line 2** runs a Builder pattern, useful for many-parameter objects, on a NeuralNetConfiguration (which can used to create a single layer if need be).

The neural net config's parameters are then assigned to an instance of the MultiLayerConfiguration object. It's useful here to disambiguate the term parameter. 

In computer science, a parameter is a value you pass into a function. In neural net parlance, parameter is usually synonymous with weight or coefficient; i.e. the parts of the model that filter, amplify or mute the input, and are adjusted as the algorithm learns. In addition, neural nets have something called hyperparameters, which are the properties of the learning algorithm that we set in a neural net configuration. In other words, the parameters mentioned above are hyperparameters, and by tuning them right you can make net's learn better and faster.  

          .layer(new RBM()) 

**Line 3** sets the layer type to RBM, or restricted Boltzmann machine, which is the shallow, building-block layer that is stacked to become a deep-belief network. 

          .nIn(numRows * numColumns) 

**Line 4** sets the number of input nodes, which are defined as the number of rows (4) by the number of columns (1) -- two variables to which values have been assigned already. Here, each row is a feature of an Iris data record: the length and width of petal and sepal. So there are four input nodes, one for each supervised feature. 

          .nOut(outputNum)

**Line 5** is the number of output nodes. We're passing in the variable outputNum, which holds a value of 3. It was set to three because there are three species of Iris flowers tracked by the dataset. The output nodes are equal to the labels you care about. 

          .seed(seed) 

**Line 6** uses a specific, randomly generated weight initialization. If you run an example many times, and generate new, random weights each time, then your net's F1 score may vary a great deal, because different initial weights can lead algorithms to different local minima of the errorscape. Keeping the weights the same allows you see the effect of adjusting other hyperparameters more clearly. 

          .visibleUnit(RBM.VisibleUnit.GAUSSIAN) 

**Line 7** applies a Gaussian transform to the RBM that makes up the net's input layer. The transform applies Gaussian white noise to normalize a distribution of continuous data. 

		.hiddenUnit(RBM.HiddenUnit.RECTIFIED) 

**Line 8** applies rectified linear to how the hidden layer samples from the visible layer. Rectified linear units (ReLU) create more robust activations and tends to improve the F1 score. These transforms look like a hockey stick, flat to begin with and then sharply rising. The flatness is the so-called offset, and ReLU applies a fixed offset to the bias of each node. That offset is a thresshold below which all samples are ignored.

          .iterations(iterations) 

**Line 9** specifies the number of iterations the algorithm will train. The number of iterations is the number of times you allow a net to classify samples and be corrected with a weight update. (Not to be confused with an epoch, which is neural-netspeak for a complete pass through the dataset.) The number of iterations represent the time you allow a net to learn: too few iterations will truncate the learning it might do; too many and you will see decreasing returns. 

          .weightInit(WeightInit.XAVIER)

**Line 10** specifies which algorithm to use on randomly generated initial weights. As the affect of pre-training on RBMs showed, proper weight initialization is crucial for RBMs' ability to learn well. [Xavier initialization](http://andyljones.tumblr.com/post/110998971763/an-explanation-of-xavier-initialization) keeps weights from becoming too small or too large over many layers. Xavier initializes a given neuron's weight by making the variance of that weight equal to one over the number of neurons feeding into it. 

          .activationFunction("relu") 

**Line 11** sets the activation function to be a rectified linear transform. So you have recitified linear appearing twice: Once on how the RBM samples from the input, and once on the nonlinear transform applied to the output. When those two overlap between hidden layers, it's effectively a passthrough that alters nothing. 

     	.l2(2e-4).regularization(true).momentum(0.9).constrainGradientToUnitNorm(true)

**Line 12** sets several parameters: 

* First, regularization is set to true. 
* L1 and L2 regularization are two ways to fight overfitting by decreasing the size of the model's weights. Here we've selected L2. 
* Momentum also known as Nesterov’s momentum, influences the speed of learning. It causes the model to converge faster to a point of minimal error. Momentum adjusts the size of the next step, the weight update, based on the previous step’s gradient. That is, it takes the gradient’s history and multiplies it. Before each new step, a provisional gradient is calculated by taking partial derivatives from the model, and the hyperparameters are applied to it to produce a new gradient. That’s where momentum influences the gradient your model uses for the next step:

					new gradient = (previous gradient * momentum) - current provisional gradient

**Line 13** sets the value of k to 1.  

          .k(1)

The k on an RBM represents the number of samples collected for each iteration. Briefly, an RBM uses Markov Chain Monte Carlo (MCMC) to calculate the gradient, as well as an algorithm called contrastive divergence that has a k parameter you control. (k = 1 is usually sufficient.)

          .lossFunction(LossFunctions.LossFunction.RMSE_XENT)

**Line 14** sets the loss function. The loss function calculates the error produced by the weights of the model, which is used to determine the gradient along which a learning algorithm adjust those weights in search of less error. Here, the loss function is root-means-squared-error-cross entropy (RMSE_XENT). RMSE is the square root of the mean of the squares of the errors. It is useful in penalizing large errors. Cross-entropy assumes predicted values (which are contrasted with the ground truth) are probabilities between 0 and 1.

          .learningRate(1e-3f)

**Line 15** sets the learning rate, which is the size of the adjustments made to the weights with each iteration. A high learning rate makes a net traverse the errorscape quickly, but makes it prone to overshoot the minima. A low learning rate is more likely to find the minimum, but it will do so very slowly.

          .optimizationAlgo(OptimizationAlgorithm.LBFGS) 

**Line 16** specifies your optimization algorithm as Limited-memory BFGS, a backpropagation method that helps calculate gradients. 

          .list(2) 

**Line 17** sets the number of neural net layers, excluding the input layer, at two. The number of layers will vary from one deep net to the other, and is an important variable to experiment with as you seek the net most appropriate for your dataset.

          .hiddenLayerSizes(3) 

**Line 18** sets the size of the hidden layers, measured in the number of nodes each contains, at three. This number can vary layer by layer, and often does. Small numbers of nodes like this are the exception -- in other deep nets, you will see layers with hundreds of nodes, or more. 

     .override(1, new ConfOverride() {
        @Override
        public void overrideLayer(int i, NeuralNetConfiguration.Builder builder) {
            builder.activationFunction("softmax");
            builder.layer(new OutputLayer());
            builder.lossFunction(LossFunctions.LossFunction.MCXENT);
        }

**Lines 19-25** override the default layer type of RBM to create an output layer that classifies input by Iris label using a multinomial sigmoid classifier called softmax. The loss function for this layer is Monte Carlo cross entropy. 

          .useDropConnect(true)

**Line 26** ensures that DropConnect is used. Drop connect, like drop out, helps a neural net generalize from training data by randomly cancelling out the interlayer edges between nodes; i.e. the channels by which the output of a node on an earlier layer is transmitted to a node on the subsequent layer. Randomly dropping information is a form of noise useful in making a model more robust.

That wraps up the deep net's configuration. Back to the rest of our program. 

### Building the Net

        .build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(listenerFreq)));

The first line above calls build on the configuration. The second passes the configuration into an instance of a MultiLayerNetwork model. The third initializes the model. The fourth sets iteration listeners, which do all kinds of neat things. 

An *iterationListener* is a hook, a plugin, which monitors the iterations and reacts to what's happening. 

A typical pattern for an iterationListener would be asking it to do something every 5 or 10 iterations. For example, you might ask it to print the error associated with your net's latest guess. You might ask it to plot either the latest weight distribution or the latest reconstructions your RBM imagines match the input data or the activations in the net itself. In addition, an iterationListener logs activity associated with the iteration, and helps you debug. 

        model.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(listenerFreq)));

In this line of code, the ScoreIterationListener is passed the parameter specifying a number of iterations -- let's say you specify 10 -- and after every 10 iterations, it will print out the error or cost. (The higher the frequency, the more you slow things down).

Next stage:

        log.info("Train model....");
        model.fit(train);

With the line above, you tell the neural net to learn, passing it the training set. 

### Evaluating the Model

Finally, we come to the evaluation stage. 

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation();
        INDArray output = model.output(test.getFeatureMatrix());

The output of the test set is a list of ground truth labels for the actual Iris species that each input sample refers to.

        for (int i = 0; i < output.rows(); i++) {
            String actual = train.getLabels().getRow(i).toString().trim();
            String predicted = output.getRow(i).toString().trim();
            log.info("actual " + actual + " vs predicted " + predicted);
        }

Here, the program prints out how well it labeled samples by each classification. 

        eval.eval(test.getLabels(), output);
        log.info(eval.stats());

Finally, we ask the program to print statistics such as accuracy and the F1 score. 

    Actual Class 0 was predicted with Predicted 0 with count 50 times

    Actual Class 1 was predicted with Predicted 1 with count 2 times

    Actual Class 1 was predicted with Predicted 2 with count 48 times

    Actual Class 2 was predicted with Predicted 2 with count 50 times

    ==========================Scores=================================
     Accuracy:  0.6667
     Precision: 0.4906
     Recall:    0.6667
     F1 Score:  0.5652272213429799
    =================================================================

In machine learning, an F1 score is a metric used to determine how well a classifier performs. It’s a number between zero and one that explains how well the network performed during training. It is analogous to a percentage, with 1 being the equivalent of 100 percent predictive accuracy. It’s basically the probability that your net’s guesses are correct. 

An F1 score accounts for issues that accuracy doesn't, because accuracy only cares about how many times the model guessed right. There are situations where mere accuracy does not accurately reflect model quality. 

Let’s say you have a class imbalance, where one category in the dataset vastly outweighs another; i.e. you have a lot of apples and very few oranges. Let’s say your model guesses “apple” 1000 times on a dataset with 1000 examples. If 999 items in your dataset are actually labeled “apple”, but the only instance of an “orange” is mislabeled by your model, it was still right 99% of the time.... 

In that case, accuracy alone won’t properly measure the quality of your model. F1 scores are better yardsticks for situations like that. The IRIS dataset is a good example, because the net in the example above is getting two classes right and the third class wrong. It's accuracy is therefore 66%, but that glosses over the fact that it's mislabeling 48 out of 50 examples for Class 1. The lower F1 score reflects that. 
