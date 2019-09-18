package org.platformfarm.knowledgebase.concurrent;

import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class ThreadInterrupted_VS_ThreadObjectIsInterrupted {

    @Test
    public void threadStaticInterruptedTest () {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    System.out.println(
                        String.format("%s >>> thread is Running"
                            , Thread.currentThread().getName()));
                    ThreadUtil.sleep(10);
                }
                System.out.println(
                    String.format("%s >>> thread is Running, but terminate shortly. anyway current thread interrupted() is %s"
                        , Thread.currentThread().getName(), Thread.interrupted()));
            }
        });

        t1.start();
        ThreadUtil.sleep(100);
        t1.interrupt();

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void threadInstanceIsInterruptedTest () {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(
                        String.format("%s >>> thread is Running", Thread.currentThread().getName()));
                    ThreadUtil.sleep(10);
                }

                System.out.println(
                    String.format("%s >>> thread is Running, but terminate shortly. anyway current thread isInterrupted() is %s"
                        , Thread.currentThread().getName(), Thread.currentThread().isInterrupted()));
            }
        });

        t1.start();
        ThreadUtil.sleep(100);
        t1.interrupt();


        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
