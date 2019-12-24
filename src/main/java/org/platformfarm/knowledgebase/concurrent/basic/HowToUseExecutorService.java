package org.platformfarm.knowledgebase.concurrent.basic;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 *
 *
 */
public class HowToUseExecutorService {

    public void newFixedThreadPoolExam() {

        ExecutorService es = Executors.newFixedThreadPool(5);
        for(int i =0; i < 10 ; i++) {
            es.submit(new SomeRunnableTask());
        }
    }

    public void newCachedThreadPoolExam() {

        ExecutorService es = Executors.newCachedThreadPool();
        for(int i =0; i < 10 ; i++) {
            es.submit(new SomeRunnableTask());
        }
    }


    class SomeRunnableTask implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread,
         * starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may take any action
         * whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            System.out.println("Current thread name: " + Thread.currentThread().getName());
        }
    }

    class SomeCallableTask implements Callable<Integer> {

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public Integer call() throws Exception {
            return 0;
        }
    }
}
