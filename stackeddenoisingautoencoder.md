---
title: 
layout: default
---

# Stacked Denoising Autoencoders

A stacked denoising autoencoder is to a denoising autoencoder what a [deep-belief network](/deepbeliefnetwork.html) is to a [restricted Boltzmann machine](../restrictedboltzmannmachine.html). A key function of SDAs, and deep learning more generally, is unsupervised pre-training, layer by layer, as input is fed through. Once each layer is pre-trained to conduct feature selection and extraction on the input from the preceding layer, a second stage of supervised fine-tuning can follow. 

A word on stochastic corruption in SDAs: Denoising autoencoders shuffle data around and learn about that data by attempting to reconstruct it. The act of shuffling is the noise, and the job of the network is to recognize the features within the noise that will allow it to classify the input. When a network is being trained, it generates a model, and measures the distance between that model and the benchmark through a loss function. Its attempts to minimize the loss function involve resampling the shuffled inputs and re-reconstructing the data, until it finds those inputs which bring its model closest to what it has been told is true. 

The serial resamplings are based on a generative model to randomly provide data to be processed. This is known as a Markov Chain, and more specifically, a Markov Chain Monte Carlo algorithm that steps through the data set seeking a representative sampling of indicators that can be used to construct more and more complex features.

### Parameters & corruption levels

See the [parameters common to all multilayer networks](../multinetwork.html). 

The amount of noise to apply to the input will take the form of a percentage. Typically 30 percent (0.3) is fine, but if you have a small amount of data, you may want to consider adding more.

### Initiating a stacked denoising autoencoder

Here's how you set up a single-thread stacked denoising autoencoder: 

To create it, you simply instantiate an object of the class [StackedDenoisingAutoEncoder](../doc/org/deeplearning4j/sda/StackedDenoisingAutoEncoder.html).

<script src="http://gist-it.appspot.com/https://github.com/SkymindIO/deeplearning4j/blob/4530b123f40645a2c34e650cbfcd6b5139638c9a/deeplearning4j-core/src/test/java/org/deeplearning4j/models/classifiers/sda/StackedDenoisingAutoEncoderTest.java?slice=35:61"></script>

This creates a stacked denoising autoencoder with the specified parameters. 

The f1 score will be a percentage. It's basically the probability that your guesses are correct. Eighty-six percent is industry standard; a solid deep-learning network should be capable of scores in the high 90s.

If you run into trouble, try modifying the hidden layer sizes, and tweaking other parameters to get the f1 score up.

Next, we'll show you how to use [distributed and multithreaded computing](../scaleout.html) to train your networks more quickly.
