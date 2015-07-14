---
layout: default
---

# What is Deeplearning4j?

Deeplearning4j is the first commercial-grade, open-source, distributed deep-learning library written for Java and Scala. Integrated with Hadoop and [Spark](../gpu_aws.html), DL4J is designed to be used in business environments, rather than as a research tool. [Skymind](http://skymind.io) is its commercial support arm.

Deeplearning4j aims to be cutting-edge plug and play, more convention than configuration, which allows for fast prototyping for non-researchers. DL4J is customizable at scale. Released under the Apache 2.0 license, all derivatives of DL4J belong to their authors. 

By following the [instructions on our Quick Start page](../quickstart.html), you can run your first examples of trained neural nets in minutes. 

### [Deep learning use cases](use_cases.html)

* [Face/image recognition](http://deeplearning4j.org/facial-reconstruction-tutorial.html)
* Voice search
* Speech-to-text (transcription)
* Spam filtering (anomaly detection)
* E-commerce fraud detection

### DL4J's main features

* A versatile [n-dimensional array](http://nd4j.org/) class. 
* [GPU](http://nd4j.org/gpu_native_backends.html) integration
* [Scalable](../scaleout.html) on [Hadoop](https://github.com/deeplearning4j/deeplearning4j/tree/master/deeplearning4j-scaleout/hadoop-yarn), [Spark](../gpu_aws.html) and Akka + AWS et al

Deeplearning4j includes both a distributed, multi-threaded deep-learning framework and a normal single-threaded deep-learning framework. Training takes place in the cluster, which means it can process massive amounts of data quickly. Nets are trained in parallel via [iterative reduce](../iterativereduce.html), and they are equally compatible with **Java**, **[Scala](http://nd4j.org/scala.html)** and **Clojure**. Deeplearning4j's role as a modular component in an open stack makes it the first deep-learning framework adapted for a [micro-service architecture](http://microservices.io/patterns/microservices.html).

### DL4J's neural nets

* [Restricted Boltzmann machines](../restrictedboltzmannmachine.html)
* [Convolutional Nets](http://deeplearning4j.org/convolutionalnets.html) (images)
* Stacked Denoising Autoencoders 
* [Recurrent Nets/LSTMs](../recurrentnetwork.html) (time series and sensor data)
* [Recursive autoencoders](https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-core/src/main/java/org/deeplearning4j/nn/layers/feedforward/autoencoder/recursive/RecursiveAutoEncoder.java)
* [Deep-belief networks](../deepbeliefnetwork.html)
* [Deep Autoencoders](http://deeplearning4j.org/deepautoencoder.html) (QA/data compression)
* Recursive Neural Tensor Networks (scenes, parsing)
* See our ["How to Choose a Neural Net" page](neuralnetworktable.html)

Deep neural nets are capable of [record-breaking accuracy](../accuracy.html). For a quick neural net introduction, please visit our [overview](../neuralnet-overview.html) page. In a nutshell, Deeplearning4j lets you compose deep neural nets from various shallow nets, each of which form a so-called layer. This flexibility lets you combine restricted Boltzmann machines, other autoencoders, convolutional nets and recurrent nets as needed in a distributed, production-grade framework that works with Spark and Hadoop on top of distributed CPUs or GPUs. 

Here's an overview of the different libraries we've built and where they fit into a larger ecosystem:

![Alt text](../img/schematic_overview.png)

There are a lot of parameters to adjust when you're training a deep-learning network. We've done our best to explain them, so that Deeplearning4j can serve as a DIY tool for Java, [Scala](https://github.com/deeplearning4j/nd4j/tree/master/nd4j-scala-api/src/main/scala/org/nd4j/api/linalg) and Clojure programmers.

If you have any questions, please join [us on Gitter](gitter.im/deeplearning4j/deeplearning4j); for premium support, [contact us at Skymind](http://www.skymind.io/contact.html). [ND4J is the Java-based scientific computing engine](http://nd4j.org/) powering our matrix operations.

![Alt text](../img/logos_8.png)
