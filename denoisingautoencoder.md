---
title: 
layout: default
---

# Denoising Autoencoders

An autoencoder is a neural network used for dimensionality reduction; that is, for feature selection and extraction. Autoencoders with more hidden layers than inputs run the risk of learning the [identity function](https://en.wikipedia.org/wiki/Identity_function) -- where the output simply equals the input -- thereby becoming useless. 

Denoising autoencoders are an extension of the basic autoencoder, and represent a stochastic version of it. Denoising autoencoders attempt to address identity-function risk by randomly corrupting input (i.e. introducing noise) that the autoencoder must then reconstruct, or denoise. 

### Parameters/corruption level 

The amount of noise to apply to the input takes the form of a percentage. Typically, 30 percent, or 0.3, is fine, but if you have very little data, you may want to consider adding more.

### Input/initiating a denoising autoencoder

Setting up a single-thread denoising autoencoder is easy. 

To create the machine, you simply instantiate an object of the class [DenoisingAutoEncoder](../doc/org/deeplearning4j/da/DenoisingAutoEncoder.html).

<script src="http://gist-it.appspot.com/https://github.com/SkymindIO/deeplearning4j/blob/4530b123f40645a2c34e650cbfcd6b5139638c9a/deeplearning4j-core/src/test/java/org/deeplearning4j/models/featuredetectors/autoencoder/DenoisingAutoEncoderTest.java?slice=20:38"></script>

That's how you set up a denoising autoencoder with one visible layer and one hidden layer using MNIST data. This net has a learning rate of 0.1, momentum of of 0.9, and utilizes reconstruction cross entropy as its loss function. 

Next, we'll show you a [stacked denoising autoencoder](../stackeddenoisingautoencoder.html), which is simply many denoising autoencoders strung together.
