package org.platformfarm.knowledgebase.concurrent.basic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 동시성을 위한 그 어떤 처리도 되지 않은 상태에서 공유자원 액세스 문제를 시뮬레이션 한다.
 *
 */
public class ConcurrencyUnconsidered {
    // 공유자원
    private int count = 0;

    public int twoThreadShareVariableIssue() {
        count = 0;
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future future1 = es.submit(new ForThreadTest());
        Future future2 = es.submit(new ForThreadTest());
        es.shutdown();

        try {
            es.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count;
    }

    class ForThreadTest implements Runnable {
        @Override public void run() {
            System.out.println(Thread.currentThread().getName());
            for(int i = 0 ; i < 10000; i++) {
                ConcurrencyUnconsidered.this.count++;
            }
        }
    }

}
