package org.platformfarm.knowledgebase.concurrent;

import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * It's a test that creates a deadlock on purpose.
 *
 *  Object A                                             Object B
 * +-----------------------------------+                +-----------------------------------+
 * | +-------------------------------+ |                | +-------------------------------+ |
 * | |critical section               | |                | |critical section               | |
 * | |                              <--------------------------------------+              | |
 * | |                               | |                | |                |              | |
 * | |               +------------------------------------>                |              | |
 * | |               |               | |                | |                |              | |
 * | +---------------|---------------+ |                | +----------------|--------------+ |
 * +-----------------|-----------------+                +------------------|----------------+
 *                   |                                                     |
 *       +----------------------+                              +----------------------+
 *       |                      |                              |                      |
 *       |       Thread 1       |                              |      Thread 2        |
 *       |                      |                              |                      |
 *       +----------------------+                              +----------------------+
 *
 * Tread 1 가 Object A 의 임계영역에 진입을 한 후 임계영역을 탈출하지 않은 상태에서 Object B의 임계
 * 영역에 진입하려 한다. 그런데 이때 이미 Thread 1는 Object B의 임계영역에 진입 한 상태가 되어
 * Thread 1 은 Object B 의 임계역역 시작 지점에서 BLOCKED 된다. 이 상황에서 Object B의 임계영역에
 * 진입한 Thread 2는 Thread 1이 Lock 을 확보한 Object A의 임계 영역을 진입하려 한다. 이 역시 BLOCKED
 * 된다. 양 Thread 는 영원한 BLOCKED 상태에 들어갔다.
 *
 * >>> 이런 상태를 교착상태라고 한다. 또는 Dead Lock 이라고 한다.
 *
 */
public class DeadLockTest {

    @Test
    public void runDeadLock() {
        // creating one object
        Shared s1 = new Shared();
        // creating second object
        Shared s2 = new Shared();

        ExecutorService es = Executors.newFixedThreadPool(3);
        Future<?> future1 = es.submit(new Task(s1, s2));
        Future<?> future2 = es.submit(new Task(s2, s1));

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
        // Returns null if no threads are deadlocked.
        long[] threadIds = bean.findDeadlockedThreads();
        if (threadIds != null) {
            ThreadInfo[] infos = bean.getThreadInfo(threadIds);
            for (ThreadInfo info : infos) {
                String s = info.getThreadName();
                System.out.println(String.format("DeadLocked Thread Name >>>> %s", s));
            }
        }
    }

    static class Shared
    {
        // first synchronized method
        synchronized void test1(Shared s2)
        {
            System.out.println("test1-begin");
            ThreadUtil.sleep(1000);

            // taking object lock of s2 enters
            // into test2 method
            s2.test2(this);
            System.out.println("test1-end");
        }

        // second synchronized method
        synchronized void test2(Shared s1)
        {
            System.out.println("test2-begin");
            ThreadUtil.sleep(1000);

            // taking object lock of s1 enters
            // into test1 method
            s1.test1(this);
            System.out.println("test2-end");
        }
    }


    static class Task implements Runnable
    {
        private Shared s1;
        private Shared s2;

        // constructor to initialize fields
        Task(Shared s1, Shared s2)
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
}