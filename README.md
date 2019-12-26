# org.platformfarm.knowledgebase.concurrent
It's a knowledge base related to concurrency.
## basic package 
동시성 프로그래밍이 필요한 기초적인 이유에 대한 정리를 한 패키지 입니다.

### HowToUseExecutorService class
ExecutorService를 이용하여 thread pool을 구성하여 사용하는 예제를 정리 하는 Class 입니다. 

![threadpoolimg](./diagram/thread-pool.png)

### ConcurrencyUnconsidered class
동시성에 대한 아무런 개념이 없는 상태에서 thread 코드를 작성하는 경우 발생하는 문제를 시뮬레이션 
하는 Class 입니다. 