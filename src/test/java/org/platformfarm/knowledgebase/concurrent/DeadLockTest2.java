package org.platformfarm.knowledgebase.concurrent;

import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Test;

public class DeadLockTest2 {
    @Test
    public void runDeadLock() {

        // creating one object
        SharedExplicitLock s1 = new SharedExplicitLock();
        // creating second object
        SharedExplicitLock s2 = new SharedExplicitLock();

        ExecutorService es = Executors.newFixedThreadPool(3);
        Future<?> future1 = es.submit(new Thread3(s1, s2));
        Future<?> future2 = es.submit(new Thread4(s1, s2));

        es.shutdown();

        try {
            int awaitCnt = 0;
            while (!es.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("Not a terminate threads. ");
                reportDeadLockThread();
                awaitCnt++;
                if ( awaitCnt > 3 && !future1.isCancelled() && !future2.isCancelled() ) {
                    future1.cancel(true);
                    future2.cancel(true);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void reportDeadLockThread() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
        if (threadIds != null) {
            ThreadInfo[] infos = bean.getThreadInfo(threadIds);
            for (ThreadInfo info : infos) {
                String s = info.getThreadName();
                System.out.println(String.format("DeadLocked Thread Name >>>> %s", s));
            }
        }
    }

}

class SharedExplicitLock
{
    private ReentrantLock lock = new ReentrantLock();

    // first synchronized method
    void test1(SharedExplicitLock s2)
    {
        try {
            lock.lockInterruptibly();
            System.out.println("test1-begin");
            ThreadUtil.sleep(1000);

            // taking object lock of s2 enters
            // into test2 method
            s2.test2(this);
            System.out.println("test1-end");

        } catch (InterruptedException e) {
            System.out.println(String.format("Release Thread >>> %s"
                , Thread.currentThread().getName()));
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    // second synchronized method
    void test2(SharedExplicitLock s1)
    {
        try {
            lock.lockInterruptibly();

            System.out.println("test2-begin");
            ThreadUtil.sleep(1000);

            // taking object lock of s1 enters
            // into test1 method
            s1.test1(this);
            System.out.println("test2-end");

        } catch (InterruptedException e) {
            System.out.println(String.format("Release Thread >>> %s"
                , Thread.currentThread().getName()));
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }
}

class Thread3 extends Thread
{
    private SharedExplicitLock s1;
    private SharedExplicitLock s2;

    // constructor to initialize fields
    public Thread3(SharedExplicitLock s1, SharedExplicitLock s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }

    // run method to start a thread
    @Override
    public void run()
    {
        // taking object lock of s1 enters
        // into test1 method
        s1.test1(s2);
    }
}


class Thread4 extends Thread
{
    private SharedExplicitLock s1;
    private SharedExplicitLock s2;

    // constructor to initialize fields
    public Thread4(SharedExplicitLock s1, SharedExplicitLock s2)
    {
        this.s1 = s1;
        this.s2 = s2;
    }

    // run method to start a thread
    @Override
    public void run()
    {
        // taking object lock of s2
        // enters into test2 method
        s2.test2(s1);
    }
}


