---
title: 
layout: default
---

# Deeplearning4j란 무엇인가요?

[Deeplearning4j](../kr-about.html)는 자바(Java)와 스칼라(Scala)를 위해 작성된 최초의 상용 등급 오픈 소스의 배포된(distributed) 딥 러닝(deep-learning) 라이브러리입니다. 하둡(Hadoop)과 스파크([Spark](../gpu_aws.html))와 통합된 DL4J는 리서치 도구이기 보다는 비즈니스 환경에서 사용할 수 있도록 설계되었습니다. 스카이마인드([Skymind](http://skymind.io))는 이의 상업적인 지원 도구 입니다.

Deeplearning4j는 설정(configuration)이기보다는 규칙(convention)을 겨냥한 최첨단 플러그 앤 플레이(plug and play)로 비연구자들에게 빠른 프로토 타이핑을 가능하게 합니다. DL4J는 규모면에서 사용자 지정이 가능합니다. 아파치(Apache) 2.0 라이센스를 사용해 출시된 DL4J의 모든 파생 상품들은 그 저자에 속합니다.

저희의 퀵 스타트([Quick Start](../kr-quickstart.html)) 페이지의 설명에 따라 여러분은 훈련된 신경망의 첫번째 예제들을 실행할 수 있습니다.

### 딥 러닝 활용 사례

* [얼굴/이미지 인식](../facial-reconstruction-tutorial.html)
* 음성 검색
* 음성-텍스트 전환 (전사)
* 스팸 필터링 (이상 탐지)
* 전자 상거래 사기 탐지

### DL4J의 주요 특징

* 다기능의 N 차원 배열 클래스([n-dimensional array class](http://nd4j.org/))
* [GPU](http://nd4j.org/gpu_native_backends.html) 통합
* 하둡, 스파크 및 Akka + AWS et al 상 확장 가능

Deeplearning4j는 배포된 멀티 스레드 딥 러닝 프레임 워크와 일반 단일 스레드 딥 러닝 프레임 워크를 모두 포함합니다. 학습은 군집(cluster)로 이뤄지며 이는 신속하게 대량의 데이터를 처리할 수 있슴을 의미합니다. 망들(nets)은 반복 감소를 통해 병렬로 학습되며, 자바, 스칼라 및 Clojure와 균일하게 호환 가능합니다. 오픈 스택의 모듈식 구성 요소로서의 Deeplearning4j의 역할이 마이크로 서비스 아키텍처에 적합한 최초의 딥 러닝 프레임 워크를 가능하게 합니다.

### DL4J의 신경망(neural nets)

* 제한 볼츠만 머신([Restricted Boltzmann machines](../restrictedboltzmannmachine.html))
* 합성곱 망([Convolutional Nets](../convolutionalnets.html)) (이미지)
* 누적된 잡음 제거용 오토인코더(Stacked Denoising Autoencoders)
* 순환 망([Recurrent Nets)/LSTMs](../recurrentnetwork.html) (시계열)
* 재귀 오토인코더(Recursive autoencoders)
* 심층 신뢰 네트워크([Deep-belief networks](../deepbeliefnetwork.html))
* 딥 오토인코더(Deep autoencoders) (QA/데이터 압축)
* 순환 뉴럴 텐서 네트워크(Neural Tensor Networks) (장면, 구문 분석)
* [저희의 "신경망을 선택하는 방법" 페이지를 참조하십시오](../neuralnetworktable.html).

심층 신경망은 기록에 남길만한 정확성을 가지고 있습니다. 신경망에 관한 간단한 소개는 저희의 개요 페이지에 있습니다. 간단히 말해서, Deeplearning4j는 소위 층(layer)이라 불리는 얕은(shallow)망으로부터 심층(deep)망을 구성할 수 있게 합니다. 이러한 유연성은 제한 볼츠만 머신, 다른 오토인코더, 합성곱 망과 순환 망을 CPU 또는 GPU 상의 스파크와 하둡과 연동할 배포된 생산 수준의 프레임 워크와 결합하게 합니다.

저희가 개발한 다른 라이브러리들과 이들이 내장된 더 큰 에코시스템의 개요는 다음과 같습니다.

![Alt text](../img/schematic_overview.png)
 
여러분이 딥 러닝 네트워크를 훈련할 때 조정해야할 많은 매개 변수들이 있습니다. 저희가 이 변수들을 설명하기 위해 최선을 다한 결과, Deeplearning4j는 자바, 스칼라 및 Clojure의 프로그래머들에게 DIY 도구로서의 역할을 다 할 수 있습니다.

질문이 있다면 Gitter를 통해; 고급지원을 원하시면 Skymind를 통해 연락주시기 바랍니다. ND4J는 저희의 상위체계 연산에 동력을 지원하는 자바 기반 과학 컴퓨팅 엔진입니다.

[한국에서의 DL4J 워크샵의 최근소식을 위해서  ujava.org 포럼에 방문을 하세요.](https://www.facebook.com/groups/ujava.org/)

![Alt text](../img/logos_8.png)
