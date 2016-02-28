---
layout: default
title: Deeplearning4j Updaters Explained
---

# Deeplearning4j Updaters Explained

We assume here that readers know how Stochastic Gradient Descent works.

The main difference in updaters is how they treat the learning rate.

## Stochastic Gradient Descent

θt+1=θt−αδL(θt)θt+1=θt−αδL(θt)

Theta (weights) is getting changed according to the gradient of the loss with respect to theta.

alpha is the learning rate. If it is very small, convergence will be very slow. On the other hand, large alpha will lead to divergence.

Now, the gradient of the loss (L) changes quickly after each iteration due to the diversity of each training example. Have a look at the convergence below. We are taking small steps but they are quite zig-zag (even though we slowly reach to a loss minima).

IMAGE

To overcome this, we introduce momentum. Basically taking knowledge from previous steps about where we should be heading. We are introducing a new hyperparameter μμ
vt+1=μvt−αδL(θt)vt+1=μvt−αδL(θt)
θt+1=θt+vt+1θt+1=θt+vt+1
We will use the concept of momentum again later.  (Don't confuse it with moment which is also used later)

IMAGE

This is the image of SGD equipped with momentum.
Adagrad:
Adagrad scales alpha for each parameter according to the history of gradients (previous steps) for that parameter which is basically done by dividing current gradient in update rule by the sum of previous gradients. As a result, what happens is that when the gradient is very large, alpha is reduced and vice-versa.
gt+1=gt+δL(θt)2gt+1=gt+δL(θt)2
θt+1=θt−αδL(θ)2gt+1‾‾‾‾√+ϵθt+1=θt−αδL(θ)2gt+1+ϵ
RMSProp:
The only difference RMSProp has with Adagrad is that the gtgtterm is calculated by exponentially decaying average and not the sum of gradients.
gt+1=γgt+(1−γ)δLgt+1=γgt+(1−γ)δL(θ)2(θ)2
Here gtgt is called the second order moment of δLδL . Additionally, a first order moment mtmt can also be introduced.
mt+1=γmt+(1−γ)δLmt+1=γmt+(1−γ)δL(θ)(θ)
gt+1=γgt+(1−γ)δLgt+1=γgt+(1−γ)δL(θ)2(θ)2
Adding momentum as in the first case,
vt+1=μvt−αδL(θ)gt+1−m2t+1+ϵ‾‾‾‾‾‾‾‾‾‾‾‾‾‾√vt+1=μvt−αδL(θ)gt+1−mt+12+ϵ
And finally collecting new theta as we have done in the first example,
θt+1=θt+vt+1θt+1=θt+vt+1
AdaDelta:
AdaDelta also uses exponentially decaying average of gtgt which was our 2nd moment of gradient. But without using alpha that we were traditionally using as learning rate, it introduces xtxt which is the 2nd moment of vtvt.
gt+1=γgt+(1−γ)▽gt+1=γgt+(1−γ)▽L(θ)2(θ)2
xt+1=γxt+(1−γ)v2t+1xt+1=γxt+(1−γ)vt+12
vt+1=−xt+ϵ‾‾‾‾‾‾√δL(θt)gt+1+ϵ‾‾‾‾‾‾‾‾√vt+1=−xt+ϵδL(θt)gt+1+ϵ
θt+1=θt+vt+1θt+1=θt+vt+1
Adam:
It uses both first order moment mtmt and 2nd order moment gtgt but they are both decayed over time. Step size is approximately ±α±α . Step size will decrease, as it approaches minimum.
mt+1=γ1mt+(1−γ1)▽mt+1=γ1mt+(1−γ1)▽L(θt)(θt)
gt+1=γ2gt+(1−γ2)▽gt+1=γ2gt+(1−γ2)▽L(θt)2(θt)2
m̂ t+1=mt+11−γt+11m^t+1=mt+11−γ1t+1
ĝ t+1=gt+11−γt+12g^t+1=gt+11−γ2t+1
θt+1=θt−αm̂ t+1ĝ t+1‾‾‾‾√+ϵ

[From Quora](https://www.quora.com/What-are-differences-between-update-rules-like-AdaDelta-RMSProp-AdaGrad-and-AdaM/answer/Rajarshee-Mitra?srid=Xs23&share=bc33d009)
