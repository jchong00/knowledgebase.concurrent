package org.platformfarm.knowledgebase.concurrent.idiom;

import net.jcip.annotations.GuardedBy;
import org.junit.Test;

public class LockOfBasicTest {


    @Test
    public void dummyTest() {

    }


    private final Object obj = new Object();

    @Test
    public void waitTest() {

        synchronized (obj) {
            try {
                obj.wait(111);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 또는 각 로직마다 필요한 적절한 예외 처리
            }
        }
    }

    @Test
    public void oneMethodTwoLock() {

        SharedObjectExam exam = new SharedObjectExam();
        exam.method1();
        exam.method2();
        exam.method3();
    }

    static class SharedObjectExam {

        final Object lockObj1 = new Object();
        final Object lockObj2 = new Object();

        @GuardedBy("SharedObjectExam#lockObj1") String sharedData1 = "홍길동";
        @GuardedBy("lockObj2") String sharedData2 = "심청이";
        @GuardedBy("this") String sharedData3 = "김복동";

        void method1() {
            synchronized (lockObj1) {
                String msg = String.format("My name is %s, thread name is %s "
                    , sharedData1, Thread.currentThread().getName());
                System.out.println(msg);
                sharedData1 = sharedData2;
            }
        }

        void method2() {
            synchronized (lockObj2) {
                String msg = String.format("My name is %s, thread name is %s "
                    , sharedData2, Thread.currentThread().getName());
                System.out.println(msg);
                sharedData2 = sharedData3;
            }
        }

        synchronized void method3() {
            String msg = String.format("My name is %s, thread name is %s "
                , sharedData3, Thread.currentThread().getName());
            System.out.println(msg);
            sharedData3 = sharedData1;
        }
    }

    @Test
    public void interruptTest() {
        DaemonServiceTypeTaskExam task = new DaemonServiceTypeTaskExam();
        Thread thread = new Thread(task);
        thread.start(); // NEW -> RUNNABLE
        try {
            Thread.sleep(3000); // Main thread는 3초만 기다리자.
        } catch (InterruptedException e) {
            // Do nothing ...
        }
        task.stopTask();
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Do nothing ...
        }
    }


    static class DaemonServiceTypeTaskExam implements Runnable  {
        boolean stopped = false;
        void stopTask() {
            stopped = true;
        }
        boolean canService() {
            return !stopped;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if ( !canService() ) // 이중 탈출 장치
                    break;

                System.out.println("Do something ....");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(String.format("%s interrupted!"
                        , Thread.currentThread().getName()));
                }
            }
            System.out.println("Will be TERMINATED ....");
        }
    }
}
