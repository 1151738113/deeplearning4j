---
title: 
layout: default
---

# Glossary

### <a name="activation">Activation</a> 
An activation for a neural network is defined as the mapping of the input to the output via a [non-linear transform function](#nonlineartransformfunction) at each "node", which is simply a locus of computation within the net. Each layer in a neural net consists of many nodes. Activation algorithms are the gates that determine, at each node in the net, whether and to what extent to transmit the signal the node has received from the previous layer. A combination of coefficients and biases work on the input data from the previous layer to determine whether that signal surpasses a given treshhold and is deemed significant. Those weights and biases are slowly altered as the neural net minimizes its error; i.e. nodes' activations change with learning.

###<a name="binarization">Binarization</a> 
The process of transforming data in to a set of zeros and ones. An example would be gray-scaling an image by transforming a picture from the 0-255 spectrum to a 0-1 spectrum. 

### <a name="confusionmatrix">Confusion Matrix</a>
Also known as an error matrix or contingency table. Confusions matrices allow you to see if your algorithm is systematically confusing two labels, by contrasting your net's predictions against a benchmark.

### <a name="contrastivedivergence">Contrastive Divergence</a>
"[Contrastive divergence](http://www.robots.ox.ac.uk/~ojw/files/NotesOnCD.pdf) is a recipe for training undirected [graphical models](#graphicalmodels) (a class of probabilistic models used in machine learning). It relies on an approximation of the [gradient](#gradient) (a good direction of change for the parameters) of the [log-likelihood](#loglikelihood) (the basic criterion that most probabilistic learning algorithms try to optimize) based on a short Markov chain (a way to sample from probabilistic models) started at the last example seen. It has been popularized in the context of Restricted Boltzmann Machines (Hinton & Salakhutdinov, 2006, Science), the latter being the first and most popular building block for deep learning algorithms." ~[*Yoshua Bengio*](http://www.quora.com/What-is-contrastive-divergence)

### <a name="downpoursgd">Downpour Stochastic Gradient Descent</a>
[Downpour stochastic gradient descent](http://research.google.com/archive/large_deep_networks_nips2012.html) is an asynchronous [stochastic gradient descent](#stochasticgradientdescent) procedure, employed by Google among others, that expands the scale and increases the speed of training deep-learning networks. 

###<a name="epoch">Epoch</a>

In machine-learning parlance, an epoch is a complete pass through a given dataset. That is, by the end of one epoch, your neural network -- be it a restricted Boltzmann machine, convolutional net or deep-belief network -- will have been exposed to every record to example within the dataset once. Not to be confused with an iteration, which is simply one update of the neural net model's parameters. Many iterations occur before an epoch is over. 

###<a name="etl">ETL</a>
Extract, transform, load: Data is loaded from disk or other sources into memory with the proper transforms such as [binarization](#binarization) and [normalization](#normalization). Broadly, you can think of a datapipeline as the process over gathering data from disparate sources and locations, putting it into a form that your algorithms can learn from, and then placing it in a data structure that they can iterate through. 

###<a name="f1">f1 Score</a>
The f1 score is a number between zero and one that explains how well the network performed during training. It is analogous to a percentage, with 1 being the equivalent of 100 percent predictive accuracy. It’s basically the probability that your net’s guesses are correct.

### <a name="feedforwardneuralnetwork">Feed-Forward Network</a>
A neural network that takes the initial input and triggers the [activation](#activation) of each layer of the network successively, without circulating. Feed-forward nets contrast with recurrent and recursive nets.

### <a name="gaussian">Gaussian Distribution</a>
A Gaussian, or [normal](https://en.wikipedia.org/wiki/Normal_distribution), distribution, is a continuous probability distribution that represents the probability that any given observation will occur on different points of a range. Visually, it resembles what's usually called a Bell curve. 

### <a name="gradient">Gradient</a>
Gradient is another word for the rate of change of a neural net as it learns how to reconstruct a dataset. That process of minimizing error is called [gradient descent](https://en.wikipedia.org/wiki/Gradient_descent). Gradient is synonymous with the idea of a derivative in [differential calculus](https://en.wikipedia.org/wiki/Differential_calculus).

### <a name="graphicalmodels">Graphical Models</a>
An undirected graphical model is another name for a [Bayesian net](https://en.wikipedia.org/wiki/Bayesian_network), which represents the probabilistic relationships between the variables represented by its nodes.

### <a name="loglikelihood">Log-Likelihood</a>
Log likelihood is related to the statistical idea of the [likelihood function](https://en.wikipedia.org/wiki/Likelihood_function#Log-likelihood). Likelihood is a function of the parameters of a statistical model. "The probability of some observed outcomes given a set of parameter values is referred to as the [likelihood](https://www.princeton.edu/~achaney/tmve/wiki100k/docs/Likelihood_function.html) of the set of parameter values given the observed outcomes."

### <a name="nonlineartransformfunction">Nonlinear Transform Function</a>  
A function that maps input on a nonlinear scale such as [sigmoid](http://en.wikipedia.org/wiki/Sigmoid_function) or [tanh](http://en.wikipedia.org/wiki/Hyperbolic_function). By definition, a nonlinear function's output is not directly proportional to its input.

###<a name="normalization">Normalization</a> 
The process of transforming the data to span a range from 0 to 1. 

###<a name="objectivefunction">Objective Function</a> 
Also called a Loss Function. An objective function is a heuristic function for reducing prediction error in a machine-learning algorithm.

###<a name="reconstructionentropy">Reconstruction Entropy</a> 
After applying Gaussian noise, a kind of statistical white noise, to the data, this [objective function](#objectivefunction) punishes the network for any result that is not closer to the original input. That signal prompts the network to learn different features in an attempt to reconstruct the input better and minimize error. 

###<a name="serialization">Serialization</a> 
Serialization is how you translate data structures or object state into storable formats. DL4J's nets are serialized, which means they can operate on devices with limited memory.

###<a name="skipgram">Skipgram</a> 
The prerequisite to a definition of skipgrams is one of ngrams. [An n-gram is a contiguous sequence of n items from a given sequence of text or speech.](https://en.wikipedia.org/wiki/N-gram) A unigram represents one "item," a bigram two, a trigram three and so forth. Skipgrams are ngrams in which the items are not necessarily contiguous. This can be illustrated best with [a few examples.](http://homepages.inf.ed.ac.uk/ballison/pdf/lrec_skipgrams.pdf) Skipping is a form of noise, in the sense of [noising and denoising](http://deeplearning4j.org/stackeddenoisingautoencoder.html), which allows neural nets to better generalize their extraction of features. See how skipgrams are implemented in [Word2vec](../word2vec.html).

### <a name="stochasticgradientdescent">Stochastic Gradient Descent</a>
[Stochastic Gradient Descent](https://en.wikipedia.org/wiki/Stochastic_gradient_descent) optimizes gradient descent and minimizes the loss function during network training. Stochastic is simply a synonym for "random."
