package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.platformfarm.knowledgebase.concurrent.GlobalUnhandledExceptionHandler;


/**
 * ExecutorService 를 사용하는 방법에 대한 예제, 주의 사항등을 정리한 Class
 *
 *
 */
public class HowToUseExecutorService {

    private static String NEW_LINE = System.lineSeparator();
    private static Map<String, String> lastExceptionThreadName = new ConcurrentHashMap<>();

    static {

        lastExceptionThreadName.put("BySubmit-Thread_", "");
        lastExceptionThreadName.put("ByExecute-Thread_", "");
        lastExceptionThreadName.put("BySubmit-Thread_2", "");
        lastExceptionThreadName.put("ByExecute-Thread_2", "");

        GlobalUnhandledExceptionHandler globalUnhandledExceptionHandler
            = new GlobalUnhandledExceptionHandler(
            (t, e) -> {

                String threadName = t.getName();

                if ( threadName.contains("BySubmit-Thread_") ) {
                    lastExceptionThreadName.replace("BySubmit-Thread_", threadName);
                } else if (threadName.contains("ByExecute-Thread_")) {
                    lastExceptionThreadName.replace("ByExecute-Thread_", threadName);
                } else if ( threadName.contains("BySubmit-Thread2_") ) {
                    lastExceptionThreadName.replace("BySubmit-Thread_", threadName);
                } else if (threadName.contains("ByExecute-Thread2_")) {
                    lastExceptionThreadName.replace("ByExecute-Thread_", threadName);
                }

                String errMsg = "### caught exception in static constructor. ###" + NEW_LINE;
                errMsg += "Occur exception... thread name: " + t.getName() + NEW_LINE;
                System.out.println(errMsg);
            });

        Thread.setDefaultUncaughtExceptionHandler(globalUnhandledExceptionHandler);
    }

    public void newFixedThreadPoolExamBySubmit() {
        ExecutorService es = Executors.newFixedThreadPool(5);
        for(int i = 0; i < 10 ; i++) {
            es.submit(new SomeRunnableTask());
        }

        es.shutdown();

        while (!es.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int newFixedThreadPoolExamBySubmitAndFuture() {
        ExecutorService es = Executors.newFixedThreadPool(5);
        Future<Integer> future = es.submit(new SomeCallableTask());
        es.shutdown();
        while (!future.isDone()) {
            try {
                // 이 스레드에서 다른 작업을 수행하는 것을 대신해 아래와 같이 sleep 을 둔다.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int result = -1;
        try {
            result = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void newFixedThreadPoolExamByExecute() {
        ExecutorService es = Executors.newFixedThreadPool(5);
        for(int i = 0; i < 10 ; i++) {
            es.execute(new SomeRunnableTask());
        }
        es.shutdown();

        while (!es.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void newCachedThreadPoolExam() {

        ExecutorService es = Executors.newCachedThreadPool();
        for(int i = 0; i < 10 ; i++) {
            es.submit(new SomeRunnableTask());
        }
    }

    /**
     *  submit 의 실행 대상이 Exception 을 throw 하면 해당 작업은 거기서 종료가 되고 thread 는 재활
     *  용 된다. pool-1-thread-5' 라는 이름의 thread 까지 생성된다. Exception 은 전파되지 않는다.
     *
     */
    public String throwsExceptionRunnableTaskUsingSubmit() {

        lastExceptionThreadName.replace("BySubmit-Thread_", "");

        ExecutorService es = Executors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "BySubmit-Thread_" + counter);
                counter++;
                return t;
            }
        });
        for(int i = 0; i < 10 ; i++) {
            Future<?> future = es.submit(new ThrowsExceptionRunnableTask());
        }

        es.shutdown();

        try {
            es.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return lastExceptionThreadName.get("BySubmit-Thread_");

    }

    /**
     *  submit 의 실행 대상이 Exception 을 throw 하면 해당 작업은 거기서 종료가 되고
     *  thread 는 재활용 된다. pool-1-thread-5' 라는 이름의 thread 까지 생성된다.
     *  Exception 은 전파되지 않는다.
     *
     *  throwsExceptionRunnableTaskUsingSubmit 와 동일한데 다만 예외 처리를 위해서
     *  ExceptionHandlingThreadPoolExecutor 를 사용하였다.
     */
    public String throwsExceptionRunnableTaskUsingSubmit2() {

        lastExceptionThreadName.replace("BySubmit-Thread2_", "");

        ExecutorService es = ExExecutors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "BySubmit-Thread2_" + counter);
                counter++;
                return t;
            }
        });

        for(int i = 0; i < 10 ; i++) {
            es.submit(new ThrowsExceptionRunnableTask());
        }

        es.shutdown();

        try {
            es.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return lastExceptionThreadName.get("BySubmit-Thread2_");

    }

    public void throwsExceptionRunnableTaskUsingSubmit3() {

        ExecutorService es = Executors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "BySubmit-Thread2_" + counter);
                counter++;
                return t;
            }
        });

        Future<?> [] futures = new Future[10];

        for(int i = 0; i < 10 ; i++) {
            futures[i] = es.submit(new ThrowsExceptionRunnableTask());
        }

        es.shutdown();

        while (!isAllFutureDone(futures)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < 10 ; i++) {
            try {
                futures[i].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        try {
            es.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isAllFutureDone (Future<?> [] futures) {
        boolean result = false;
        for(Future<?> f : futures ) {
            result |= f.isDone();
        }
        return result;
    }

    /**
     *  execute 의 실행 대상이 Exception 을 throw 하면 해당 thread 가 깨진다.
     *  해서 'pool-1-thread-10' 라는 이름의 thread 까지 생성된다.
     *  해당 Exception 은 전파된다.
     */
    public String throwsExceptionRunnableTaskUsingExecute() {

        lastExceptionThreadName.replace("ByExecute-Thread_", "");

        ExecutorService es = Executors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "ByExecute-Thread_" + counter);
                counter++;
                return t;
            }
        });

        for(int i = 0; i < 10 ; i++) {
            es.execute(new ThrowsExceptionRunnableTask());
        }

        es.shutdown();

        try {
            es.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return lastExceptionThreadName.get("ByExecute-Thread_");
    }

    public String throwsExceptionRunnableTaskUsingExecute2() {
        lastExceptionThreadName.replace("ByExecute-Thread2_", "");

        ExecutorService es = ExExecutors.newFixedThreadPool(5, new ThreadFactory() {
            private int counter = 1;
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = new Thread(runnable, "ByExecute-Thread2_" + counter);
                counter++;
                return t;
            }
        });

        for(int i = 0; i < 10 ; i++) {
            es.execute(new ThrowsExceptionRunnableTask());
        }

        es.shutdown();

        try {
            es.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return lastExceptionThreadName.get("ByExecute-Thread2_");
    }

    /**
     * Runnable 방식의 thread Task 정의
     *
     */
    static class SomeRunnableTask implements Runnable {

        @Override
        public void run() {
            System.out.println("Current thread name: " + Thread.currentThread().getName());
        }
    }

    /**
     * Runnable 방식의 thread Task 정의, 실행하면서 Runtime Exception 이 throw 된다.
     *
     */
    static class ThrowsExceptionRunnableTask implements Runnable {

        @Override
        public void run() {
            System.out.println("Current thread name: " + Thread.currentThread().getName());
            Integer intResult = Integer.parseInt("삼"); // occurs exception ...
            // 실행 되지 않음
            System.out.println("This code exists after the code causing the error.");
        }
    }

    /**
     * Callable<T> 방식의 thread Task 정의
     *
     */
    static class SomeCallableTask implements Callable<Integer> {

        @Override
        public Integer call() {
            System.out.println("Current thread name: " + Thread.currentThread().getName());
            return 0;
        }
    }
}
