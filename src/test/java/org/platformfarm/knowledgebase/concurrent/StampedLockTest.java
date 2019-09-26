package org.platformfarm.knowledgebase.concurrent;

import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.StampedLock;
import org.junit.Test;

public class StampedLockTest {

    @Test
    public void wrongStampUsageInStampedLockTest() {

        int cnt = 0;
        StampedLock sl = new StampedLock();

        long stamp = sl.writeLock();
        try {
            cnt++;
        } finally {
            try {
                sl.unlockWrite(stamp+1); // 일부러 다른 숫자를 넣어 봤다.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Test
    public void stampedLockBadCaseTest() {
        StampedLockBadCaseTest test = new StampedLockBadCaseTest();
        test.entryMethod();
    }

    static class StampedLockBadCaseTest {
        private int cnt = 0;
        private final StampedLock sl = new StampedLock();
        void entryMethod() {
            long stamp = sl.writeLock();
            try {
                cnt++;
                System.out.println(String.format("Current count >>> %d", cnt));
                ThreadUtil.sleep(100);
                nextDeadLockMethod();
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        void nextDeadLockMethod() {
            long stamp = sl.writeLock();
            try {
                cnt++;
                System.out.println(String.format("Current count >>> %d", cnt));
                ThreadUtil.sleep(100);
            } finally {
                sl.unlockWrite(stamp);
            }
        }

    }

    @Test
    public void reentrantLockReenterTest() {
        ReentrantLockReenterObj test = new ReentrantLockReenterObj();
        test.entryMethod();
    }

    @Test
    public void eachConditionTest() {
        ReentrantLockReenterObj test = new ReentrantLockReenterObj();
        Condition[] condi1 = new Condition[1];
        Thread t1 = new Thread(()->{
            test.newConditionMethod(condi1);
        });
        t1.start();
        ThreadUtil.sleep(100);
        test.callSignal(condi1[0]);

        Condition[] condi2 = new Condition[1];
        Thread t2 = new Thread(()->{
            test.newConditionMethod(condi2);
        });
        t2.start();
        ThreadUtil.sleep(3000);
        test.callSignal(condi2[0]);

    }

    static class ReentrantLockReenterObj {
        private final ReentrantLock reentrantLock = new ReentrantLock();
        void newConditionMethod(Condition[] condi) {
            reentrantLock.lock();
            try {
                condi[0] = reentrantLock.newCondition();
                try {
                    condi[0].await();
                    SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm:ss");
                    String formattedTime = dateFormat.format (System.currentTimeMillis());
                    System.out.println(String.format("%s 깨어남! 현재 시각: %s"
                        , Thread.currentThread().getName(), formattedTime));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                reentrantLock.unlock();
            }
        }

        void callSignal(Condition condi) {
            reentrantLock.lock();
            try {
                condi.signalAll();
            } finally {
                reentrantLock.unlock();
            }
        }

        private int cnt = 0;


        void entryMethod() {
            reentrantLock.lock();
            try {
                cnt++;
                System.out.println(String.format("Current count >>> %d", cnt));
                ThreadUtil.sleep(100);
                nextDeadLockMethod();
            } finally {
                reentrantLock.unlock();
            }
        }

        void nextDeadLockMethod() {
            reentrantLock.lock();
            try {
                cnt++;
                System.out.println(String.format("Current count >>> %d", cnt));
                ThreadUtil.sleep(100);
            } finally {
                reentrantLock.unlock();
            }
        }

    }

    @Test
    public void oracleDocExamCodeTest1() {
        Point point = new Point();
        ExecutorService es = createExecutorService(2);
        Future<?> future1 = es.submit(new ThreadTaskForTest1(point));
        Future<?> future2 = es.submit(new ThreadTaskForTest1(point));
        point.moveIfAtOrigin(1,1);
        ThreadUtil.sleep(5);
        for (int i = 2 ; i < 100; i++) {
            point.move(i, i);
            ThreadUtil.sleep(5);
        }
    }

    private ExecutorService createExecutorService(final int threadSize) {
        final ThreadFactory factory = new ThreadFactory() {
            private int counter;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "THREAD_FOR_TEST-" + counter++ );
                t.setDaemon(true);
                return t;
            }
        }; return Executors.newFixedThreadPool(threadSize, factory);
    }

    static class ThreadTaskForTest1 implements Runnable {
        private Point point;
        ThreadTaskForTest1 (Point point) {
            this.point = point;
        }
        @Override
        public void run() {
            int cnt = 0;
            while (!Thread.currentThread().isInterrupted()) {
                cnt++;
                if (cnt > 100)
                    break;

                double distance = this.point.distanceFromOrigin();
                System.out.println(String.format("%s Thread's print, Distance of Point : %.5f"
                    , Thread.currentThread().getName(), distance));
                ThreadUtil.sleep(5);
            }

        }
    }


    // Under the class is taken form oracle site
    static class Point {
        private double x, y; // 보호되어야 할 공유 객체
        private final StampedLock sl = new StampedLock();

        void move(double deltaX, double deltaY) { // an exclusively locked method
            long stamp = sl.writeLock();
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                sl.unlockWrite(stamp);
            }
        }

        double distanceFromOrigin() { // A read-only method
            long stamp = sl.tryOptimisticRead();
            double currentX = x, currentY = y;
            if (!sl.validate(stamp)) { // 누가 Lock 을 가져가 stamp 가 변했다.
                stamp = sl.readLock();
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    sl.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }

        void moveIfAtOrigin(double newX, double newY) { // upgrade, 즉 읽기 락이 쓰기 락으로 변경
            // Could instead start with optimistic, not read mode
            // 읽기모드 대신 낙관적인 모드로 시작할 수 있다. (distanceFromOrigin 처럼)
            long stamp = sl.readLock();
            try {
                while (x == 0.0 && y == 0.0) {
                    long ws = sl.tryConvertToWriteLock(stamp);
                    if (ws != 0L) {
                        stamp = ws;
                        x = newX;
                        y = newY;
                        break;
                    }
                    else { // lock 을 획득하지 못함, 기다리지 않음 기존 읽기 Lock 을 해제 하고
                           // 쓰기 Lock 획득
                        sl.unlockRead(stamp);
                        stamp = sl.writeLock();
                    }
                }
            } finally {
                sl.unlock(stamp);
            }
        }
    }

}
