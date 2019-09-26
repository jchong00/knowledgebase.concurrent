package org.platformfarm.knowledgebase.concurrent;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class ThreadMonitoringTest {

    @Test
    public void getTreadInfo() {

        class LongTermTask {
            void longTermMethod() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    System.out.println(">>> sleep2 interrupted!!");
                    Thread.currentThread().interrupt();
                }
            }
        }


        Thread t1 = new Thread(()->{
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println(
                    String.format("%s is Running!!!", Thread.currentThread().getName()));

                LongTermTask longTermTask = new LongTermTask();
                longTermTask.longTermMethod();

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println(">>> sleep1 interrupted!!");
                    Thread.currentThread().interrupt();
                }

            }
        });

        t1.start();
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        ThreadInfo threadInfo = tmx.getThreadInfo(t1.getId());
        System.out.println(threadInfo.toString());
        t1.interrupt();
    }


    @Test
    public void getTreadInfoGetLockedMonitors() {

        class SharedLongTermTask {

            synchronized void longTermMethod() {
                System.out.println(String.format(">>> Enter the Critical section(longTermMethod) %s"
                    , Thread.currentThread().getName()));
                try {
                    longTermMethodAnother();
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    System.out.println(">>> sleep interrupted!!");
                    Thread.currentThread().interrupt();
                }

            }

            synchronized void longTermMethodAnother() {
                System.out.println(String.format(">>> Enter the Critical section(longTermMethodAnother) %s"
                    , Thread.currentThread().getName()));
                System.out.println(">>> sleep interrupted!!");
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    System.out.println(">>> sleep interrupted!!");
                    Thread.currentThread().interrupt();
                }

            }

        }

        SharedLongTermTask longTermTask = new SharedLongTermTask();

        class MainTestTask implements Runnable {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(
                        String.format("%s is Running!!!", Thread.currentThread().getName()));
                    longTermTask.longTermMethod();
                }
            }
        }

        class MainTestTaskAnother implements Runnable {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println(
                        String.format("%s is Running!!!", Thread.currentThread().getName()));
                    longTermTask.longTermMethodAnother();
                }
            }
        }

        Thread t1 = new Thread(new MainTestTask());
        Thread t2 = new Thread(new MainTestTask());

        t1.start();
        ThreadUtil.sleep(100);
        t2.start();
        ThreadUtil.sleep(500);
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();

        ThreadInfo[] threadInfo1 = tmx.getThreadInfo(new long[]{t1.getId()}
        , true, true);

        System.out.print(threadInfo1[0].toString());
        System.out.println("getBlockedCount() >>>  " + threadInfo1[0].getBlockedCount());

        ThreadInfo[] threadInfo2 = tmx.getThreadInfo(new long[]{t2.getId()});
        System.out.print(threadInfo2[0].toString());
        System.out.println("getBlockedCount() >>>  " + threadInfo2[0].getBlockedCount());

        MonitorInfo[] monitorInfos1 = threadInfo1[0].getLockedMonitors();
        System.out.println("getLockedMonitors() result size >>> " + monitorInfos1.length);

        for(MonitorInfo mi : monitorInfos1) {
            System.out.println("MonitorInfo getClassName %s >>> " + mi.getClassName());
            System.out.println("MonitorInfo getMethodName %s >>> " + mi.getLockedStackFrame().getMethodName());
        }

        LockInfo[] lockInfos = threadInfo1[0].getLockedSynchronizers();
        System.out.println("getLockedSynchronizers() result size >>> " + lockInfos.length);

        t1.interrupt();
        t2.interrupt();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
