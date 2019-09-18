package org.platformfarm.knowledgebase.concurrent;

import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;

import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

/**
 *
 *
 *
 */
public class ThreadWaitAndNotifyTest {

    @Test
    public void waitNotifyControlTest() {

        // 람다식이나 익명객에에서 지역 변수를 사용하면 그 스코프 내에서는 final 이 되어버린다.
        // 이를 회피하기 위해 배열 한 칸 짜리를 선언하고 그 요소를 접근하여 사용한다.
        final Thread[] testThreads = new Thread[1];
        testThreads[0] = new Thread(() -> {
            int i = 0;
            while (!testThreads[0].isInterrupted()) {
                Clock clock = Clock.systemUTC();
                System.out
                    .println(String.format("Test Thread running >>>> %s%n", clock.instant()));
                ThreadUtil.sleep(500);
                i++;
                if (i > 10)
                    break;

            }

            // 작업이 모두 끝나면 대기중인 다른 Thread 를 깨우기 위해 notify() 를 호출한다.
            synchronized (testThreads[0]) {
                testThreads[0].notify(); // notify는 Thread에 속한 함수가 아닌 Object에 속한 함수다.
            }

            System.out.println("CHECK POINT >>>> after working loop in new thread");
        });

        testThreads[0].start();
        System.out.println("CHECK POINT >>>> after start() in main thread.");

        synchronized (testThreads[0]) { // wait, notify 모두 동기화 블럭내에서 사용해야 한다.
            try {
                System.out.println("CHECK POINT >>>> before wait() in main thread.");
                // 현재 Thread 를 testThreads[0] 객체에 wait 를 위탁시킨다. 어느 자료에서는 해당객체
                // 가 제공하는 휴게실에 들어간다는 표현을 했다. 여기서 Thread 인스턴스 변수를 사용
                // 했으나 꼭 그렇게 하지 않아도 된다. 일반변수 하나를 선언하여 써도 된다.
                testThreads[0].wait(); // 숫자(시간)을 지정하지 않았으니 계속 기다린다.
                System.out.println("CHECK POINT >>>> after wait() in main thread.");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void waitNotifyDozenControlTest() {

        Object threadLockKey = new Object();

        final Thread[] testThreads = new Thread[2];
        testThreads[0] = new Thread(() -> {
            int i = 0;
            while (!testThreads[0].isInterrupted()) {
                Clock clock = Clock.systemUTC();
                System.out
                    .println(String.format("Test Thread running >>>> %s%n", clock.instant()));
                ThreadUtil.sleep(500);
                i++;
                if (i > 10)
                    break;

            }

            testThreads[1].interrupt();

            //
            // 위 interrupt() 코드와 아래 notify 코드를 연달아 수행하면 위 interrupt 코드가 무효화
            // 된다. 왜 그런지 아직 모른다.
            //
//            synchronized (threadLockKey) {
//                threadLockKey.notify();
//            }

            System.out.println("CHECK POINT >>>> after working loop in new thread");
        });

        testThreads[1] = new Thread(() -> {
            synchronized (threadLockKey) {
                try {
                    threadLockKey.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("CHECK POINT >>>> after wait() in new thread 2");
            synchronized (threadLockKey) {
                threadLockKey.notify();
            }

        });

        testThreads[0].start();
        testThreads[1].start();
        System.out.println("CHECK POINT >>>> after start() in main thread.");

        synchronized(threadLockKey) {
            try {
                threadLockKey.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (threadLockKey) {
            threadLockKey.notify();
        }

        System.out.println("CHECK POINT >>>> after wait() in main thread.");
    }

    @Test
    public void waitNotifyWrongCaseTest() {

        Object threadLockKey = new Object();

        final Thread[] testThreads = new Thread[2];
        testThreads[0] = new Thread(() -> {
            synchronized (threadLockKey) {
                try {
                    threadLockKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("CHECK POINT >>>> after wait() in new thread 1");
            synchronized (threadLockKey) {
                threadLockKey.notify();
            }
        });

        testThreads[1] = new Thread(() -> {
            synchronized (threadLockKey) {
                try {
                    threadLockKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("CHECK POINT >>>> after wait() in new thread 2");
            synchronized (threadLockKey) {
                threadLockKey.notify();
            }

        });

        testThreads[0].start();
        testThreads[1].start();
        System.out.println("CHECK POINT >>>> after start() in main thread.");

        try {
            Thread.sleep(5000);
            System.out.println("CHECK POINT >>>> before notify all.");
            reportStarvationThread();
            synchronized (threadLockKey) {
                threadLockKey.notifyAll();
            }
            System.out.println("CHECK POINT >>>> after notify all.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //
        // 사실 아래코드 같이 Main-Thread 까지 wait 를 하면 이 프로그램 자체는 DeadLock 상태에 빠지
        // 게 된다.
        //

//        synchronized(threadLockKey) {
//            try {
//                threadLockKey.wait();
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        synchronized (threadLockKey) {
//            threadLockKey.notify();
//        }

//        System.out.println("CHECK POINT >>>> after wait() in main thread.");

    }

    private void reportDeadLockThread() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
        if (threadIds != null) {
            ThreadInfo[] infos = bean.getThreadInfo(threadIds);
            for (ThreadInfo info : infos) {
                String s = info.getThreadName();
                System.out.println(String.format("DeadLocked Thread Name >>>> %s", s));
                //StackTraceElement[] stack = info.getStackTrace();
                // Log or store stack trace information.
            }
        }
    }

    private void reportStarvationThread() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] threadIds = bean.getAllThreadIds();
        ThreadInfo[] infos = bean.getThreadInfo(threadIds);
        for (ThreadInfo info : infos) {

            if ( info.getWaitedCount() > 0
                && ( info.getThreadState() == WAITING || info.getThreadState() == TIMED_WAITING)) {
                String s = info.getThreadName();
                System.out.println(String.format("Starvation Thread Name >>>> %s", s));
            }
        }
    }

    @Test
    public void waitNotifyAllControlTest() {

        Object threadLockKey = new Object();

        final Thread[] testThreads = new Thread[2];
        testThreads[0] = new Thread(() -> {
            int i = 0;
            while (!testThreads[0].isInterrupted()) {
                Clock clock = Clock.systemUTC();
                System.out
                    .println(String.format("Test Thread running >>>> %s%n", clock.instant()));
                ThreadUtil.sleep(500);
                i++;
                if (i > 10)
                    break;

            }

            synchronized (threadLockKey) {
                threadLockKey.notifyAll();
            }

            System.out.println("CHECK POINT >>>> after working loop in new thread");
        });

        testThreads[1] = new Thread(() -> {
            synchronized (threadLockKey) {
                try {
                    threadLockKey.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("CHECK POINT >>>> after wait() in new thread 2");
        });

        testThreads[0].start();
        testThreads[1].start();
        System.out.println("CHECK POINT >>>> after start() in main thread.");

        synchronized(threadLockKey) {
            try {
                threadLockKey.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("CHECK POINT >>>> after wait() in main thread.");
    }

    @Test
    public void sleep_VS_waitTest() {
        Object threadLockKey = new Object();
        final Thread[] testThreads = new Thread[2];

        testThreads[0] = new Thread(() -> {
            int i = 0;
            while (!testThreads[0].isInterrupted()) {
                Clock clock = Clock.systemUTC();
                System.out.println(String.format("%s running >>>> %s%n"
                    , Thread.currentThread().getName()
                    , clock.instant()));
                ThreadUtil.sleep(500);

                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                ThreadInfo info = bean.getThreadInfo(testThreads[1].getId());

                if ( info.getThreadState() == TIMED_WAITING ) {
                    System.out.println(String.format("%s is Timed Waiting >>>> %n"
                        , Thread.currentThread().getName()));
                }


                i++;
                if (i > 5) {
                    testThreads[1].interrupt();
                    break;
                }


            }
            System.out.println("CHECK POINT >>>> after working loop in new thread-1");
        });

        testThreads[1] = new Thread(() -> {
            int i = 0;

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println(String.format("Current thread is Interrupted? %b%n"
                    , Thread.currentThread().isInterrupted()));
                Thread.currentThread().interrupt();
                System.out.println(String.format("Current thread is Interrupted? %b%n"
                    , Thread.currentThread().isInterrupted()));
            }

            while (!testThreads[1].isInterrupted()) {

                Clock clock = Clock.systemUTC();
                System.out.println(String.format("%s running >>>> %s%n"
                        , Thread.currentThread().getName()
                        , clock.instant()));
                ThreadUtil.sleep(500);
                i++;
                if (i > 10)
                    break;

            }

            synchronized (threadLockKey) {
                threadLockKey.notifyAll();
            }

            System.out.println("CHECK POINT >>>> after working loop in new thread-2");
        });

        testThreads[0].setName("Thread Test - 01");
        testThreads[1].setName("Thread Test - 02");
        testThreads[0].start();
        testThreads[1].start();
        System.out.println("CHECK POINT >>>> after start() in main thread.");
        testThreads[1].interrupt();

        synchronized(threadLockKey) {
            try {
                threadLockKey.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("CHECK POINT >>>> after wait() in main thread.");
    }

    @Test
    public void yieldTest () {

        Object threadLockKey = new Object();
        final Thread[] testThreads = new Thread[2];
        AtomicBoolean isDoneSecondThread = new AtomicBoolean(false);
        testThreads[0] = new Thread (() -> {
            while (!isDoneSecondThread.get()) {
                System.out.println("First thread is will yield ... ");
                Thread.yield();
            }
        } );

        testThreads[1] = new Thread (() -> {
            int i = 0;
            while (!testThreads[1].isInterrupted()) {
                Clock clock = Clock.systemUTC();
                System.out
                    .println(String.format("Test Thread running >>>> %s%n", clock.instant()));
                i++;
                if (i > 10)
                    break;

            }

            boolean success = isDoneSecondThread.compareAndSet(false, true);
            if ( success ) {
                System.out.println("Set success AtomicBoolean flags.");
            }

            synchronized(threadLockKey) {
                threadLockKey.notifyAll();
            }

        });
        testThreads[0].start();
        testThreads[1].start();

        synchronized(threadLockKey) {
            try {
                threadLockKey.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("CHECK POINT >>>> after wait() in main thread.");
    }

    @Test
    public void whatIsDeadLock() {

        ExecutorService es = Executors.newFixedThreadPool(3);
        Future<?> future1 = es.submit(new DeadLockTask());
        Future<?> future2 = es.submit(new DeadLockTask());

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

    static class DeadLockTask implements Runnable {

        boolean stopFlag = false;

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
            synchronized (DeadLockTask.class) {
                while (!Thread.currentThread().isInterrupted()) {
                    if (stopFlag) {
                        break;
                    }
                    System.out.println(String.format("Do something in %s task!!"
                        , Thread.currentThread().getName()));
                    try {
                        Thread.sleep(10);
                        System.out.println("Wait released...");
                    } catch (InterruptedException e) {
                        System.out.println(String.format("%s Thread is interrupted...."
                            , Thread.currentThread().getName()));
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}

