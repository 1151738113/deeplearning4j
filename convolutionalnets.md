---
title: 
layout: default
---

# Convolutional Networks

Convolutional nets perform object recognition with images. They can identify faces, individuals, street signs, eggplants, platypuses and many other aspects of visual data. Convolutional nets overlap with text analysis via optical character recognition, but they are also useful when analyzing words as discrete textual units, as well as sound. 

The efficacy of convolutional nets (ConvNets) in image recognition is one of the main reasons why the world has woken up to deep learning. They are powering major advances in machine vision, which has obvious applications for self-driving cars, robotics, drones, and treatments for the visually impaired. 

From the Latin, *to convolve* is to roll together. For mathematical purposes, a convolution is a integral measuring how much two functions overlap as one passes over the other. Think of a convolution as a way of mixing two functions by multiplying them. 

Imagine a tall, narrow bell curve standing in the middle of a graph. The integral is the area under that curve. Now imagine a second bell curve that is shorter and wider, drifting slowly from the left side of the graph to the right. The product of those two functions at each point along the x-axis is their [convolution](http://mathworld.wolfram.com/Convolution.html). S two functions are being "rolled together."

The static, underlying function is the input image being analyzed, and the second, mobile function is known as the filter, because it picks up the signal of the image. The two functions relate through multiplication. To visualize convolutions as matrices rather than as bell curves, please see [Andrej Karpathy's excellent animation](https://cs231n.github.io/convolutional-networks/) under the heading "Convolution Demo."

The next thing to understand about convolutional nets is that they are passing many filters for the image, each one picking up different signals. One way to imagine it would be to think of them passing a horizontal line filter, a vertical line filter, and a diagonal line filter to create a map of the edges in the image. 

Convolutional nets take those slices of the feature space of an image and learn them one by one. By learning different portions of a feature space, convolutional nets allow for easily scalable and robust feature engineering.

(Note that convolutional nets analyze images differently than RBMs. While RBMs learn to reconstruct and identify the features of each image as a whole, convolutional nets learn images in pieces that we call feature maps.) 

So convolutional networks perform a sort of search. Picture a small magnifying glass sliding left to right across a larger image, and recommencing at the left once it reaches the end of one pass (like typewriters do). That moving window is capable recognizing only one thing, say, a short vertical line. Three dark pixels stacked atop one another. It moves that vertical-line-recognizing filter over the actual pixels of the image, looking for matches.

Each time a match is found, it is mapped onto a feature space particular to that visual element. In that space, the location of each vertical line match is recorded, a bit like birdwatchers leave pins in a map to mark where they last saw a great blue heron. A convolutional net runs many, many searches over a single image – horizontal lines, diagonal ones, as many as there are visual elements to be sought. 

![Alt text](../img/convnet.png) 

Convolutional nets perform more operations on input than just convolutions themselves. 

After a convolutional layer, input is passed through a nonlinear transform such as *tanh* or *rectified linear* unit, which will squash input values into a range between -1 and 1. 

Then the signal progresses to the next major stage of convolutional nets: max pooling, also known as downsampling. Downsampling, by definition, reduces the volume of information passing through the net, which is important, since images are costly to process.

Max pooling aggregates the feature maps (subsections of subsections) onto one space to get an overall “expectation” of where features occur. This expectation is then projected onto a 2D space relative to the hidden layer size of the convolutional layer.

Only the locations on the image that showed the strongest correlation to the feature (the maximum value) are preserved, and those maximum values are combined in a lower-dimensional space. 

Much information about lesser values is lost in this step, also known as downsampling, and that has spurred research into alternative methods. But downsampling has the advantage, precisely because information is lost, of decreasing the amount of storage and processing required by the net. 

### DL4J Code Example

Here's one example of how you might configure a ConvNet with Deeplearning4j:

<script src="http://gist-it.appspot.com/https://github.com/deeplearning4j/dl4j-0.0.3.3-examples/blob/master/src/main/java/org/deeplearning4j/convolution/CNNMnistExample.java?slice=32:100"></script>

### ConvNets in Academia

[Yann LeCun](http://yann.lecun.com/exdb/publis/pdf/lecun-iscas-10.pdf), a professor at New York University and director of research at Facebook, has done much to advance and promote the use of convolutional nets, which are used heavily in machine vision tasks. 

[Andrej Karpathy's Stanford course](https://cs231n.github.io/) on Convolutional Nets is fantastic. We highly recommend it as an introduction to the major ideas. (*Exercises in Python.*)

[Here's our Github test for ConvNets](https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-core/src/test/java/org/deeplearning4j/models/layers/ConvolutionDownSampleLayerTest.java).

 <script src="http://gist-it.appspot.com/https://github.com/deeplearning4j/deeplearning4j/blob/master/deeplearning4j-core/src/test/java/org/deeplearning4j/models/layers/ConvolutionDownSampleLayerTest.java?slice=55:99"></script>
