---
title: 
layout: default
---

# Deeplearning4j's Vizualization and UI

The deeplearning4j-ui repository can display T-SNE, histograms, filters and activations. 

To support visualizations that will help you monitor neural networks as they learn, and therefore debug them, you must set up an iteration listener. This is done when you instantiate and initialize any new MultiLayerNetwork.

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(Arrays.asList((IterationListener) new ScoreIterationListener(listenerFreq)));

The first line above passes a configuration you will have specified previously into an instance of a MultiLayerNetwork model. The second initializes the model. The third sets iteration listeners. Remember, an iteration is simply one update of a network's weights: you may decide to update the weights after a batch of examples is processed, or you may update them after a full pass through the dataset, known as an epoch.

An *iterationListener* is a hook, a plugin, which monitors the iterations and reacts to what's happening. 

A typical pattern for an iterationListener would be asking it to do something every two to five iterations. For example, you might ask it to print the error associated with your net's latest guess. You might ask it to plot either the latest weight distribution or the latest reconstructions that your RBM imagines match the input data, or the activations in the net itself. In addition, an iterationListener logs activity associated with the iteration, and helps you debug. 

        model.setListeners(Collections.singletonList((IterationListener) new ScoreIterationListener(listenerFreq)));

In this line of code, the ScoreIterationListener is passed the parameter specifying a number of iterations -- let's say you specify two -- and after every two iterations, it will print out the error or cost. (Caveat: The more often you call iterationListener, the slower your training will run...).

## UI in the Browser

A [UI server](https://github.com/deeplearning4j/deeplearning4j/blob/f0688a59bb712dc9d3b9eefa191a5f521bab27d0/deeplearning4j-ui/src/main/java/org/deeplearning4j/ui/UiServer.java) should start automatically with [Jetty](https://en.wikipedia.org/wiki/Jetty_(web_server)) and the results will appear here: [http://localhost:8080/weights](http://localhost:8080/weights). 

(If you need to change something manually, you can run the server as a main class in Intellij or Eclipse. Alternatively, you can use `java -cp` with the right classpath and specify: `org.deeplearning4j.ui.UiServer`. That starts a Jetty server with UI functionality.)

You can see the code for the [HistogramIterationListener here](https://github.com/deeplearning4j/deeplearning4j/blob/9ca18d8f0b4828a55f381d50e32b6eebcb3444e0/deeplearning4j-ui/src/main/java/org/deeplearning4j/ui/weights/HistogramIterationListener.java#L35-34). You would specify `HistogramIterationListener` rather than `ScoreIterationListener`, as in the line of code below:

        model.setListeners(Collections.singletonList((IterationListener) new HistogramIterationListener(listenerFreq)));

Here are some of the file names you'll want to get familiar with in the deeplearning4j-ui repo: 

* deeplearning4j-ui/src/main/java/org/deeplearning4j/ui/weights/HistogramIterationListener.java
* deeplearning4j-ui/src/main/java/org/deeplearning4j/ui/weights/ModelAndGradient.java 
* deeplearning4j-ui/src/main/resources/org/deeplearning4j/ui/weights/render.ftl 
