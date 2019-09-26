package org.platformfarm.knowledgebase.concurrent.idiom;


import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class BoundedHashSetTest {

    /**
     * 문제점 발견: 아래와 같은 조건, 즉 Thread pool 을 사용하고 Take 에 isInterrupted
     * 또는 interrupted 로 탈출 검사하는 무한 Loop 가 있을때 내부에 System.out.println 이 존재하는
     * 경우 isInterrupted, interrupted 가 true 로 변경되는 작업이 무시되고 이유는 알수 없지만 1이라
     * 도 sleep 을 걸게 되면 문제가 사라진다. 또 다른 걱정: 이런 문제를 일으킬수 있은 또 다른 요인이
     * 있지않을까?
     */
    @Test
    public void producer2AndConsumer1() {

        BoundedHashSet<String> boundedHashSet = new BoundedHashSet<>(20);

        class ProducerTask implements Runnable {
            @Override
            public void run() {
                int rpt = 0;
                while (!Thread.interrupted()) {
                    rpt++;
                    try {
                        boundedHashSet.add("TEST-" + rpt);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("TERMINATE >>> ProducerTask");
            }
        }

        class ConsumerTask implements Runnable {
            @Override
            public void run() {
                int rpt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    rpt++;
                    boolean done = boundedHashSet.remove("TEST-" + rpt);
                    System.out.println(String.format("Remains >>> %s, %s, %s"
                        , boundedHashSet.getRemainedPermits(), done, Thread.currentThread().isInterrupted()));
                    ThreadUtil.sleep(1);
                    if (Thread.currentThread().isInterrupted())
                        break;
                }
                System.out.println("TERMINATE >>> ConsumerTask");
            }
        }


        ExecutorService es = createExecutorService(2);// Executors.newFixedThreadPool(2);

        Future<?> future1 = es.submit(new ProducerTask());
        Future<?> future3 = es.submit(new ConsumerTask());
        es.shutdown();

        try {
            es.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        future1.cancel(true);
        future3.cancel(true);


        System.out.println("CALLED >>> future1, future3 interrupt!!!");

        ThreadUtil.sleep(100); // 메시지를 보기위해서 잠깐 쉰다.

    }

    private ExecutorService createExecutorService(final int count) {
        final ThreadFactory factory = new ThreadFactory() {
            private int counter;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "MyTestThread_" + counter++ );
                t.setDaemon(true);
                return t;
            }
        }; return Executors.newFixedThreadPool(count, factory);
    }

    @Test
    public void producer2AndConsumer1_t() {

        BoundedHashSet<String> boundedHashSet = new BoundedHashSet<>(20);

        class ProducerTask implements Runnable {
            @Override
            public void run() {
                int rpt = 0;
                while (!Thread.interrupted()) {
                    rpt++;
                    try {
                        boundedHashSet.add("TEST-" + rpt);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                System.out.println("TERMINATE >>> ProducerTask");
            }
        }

        class ConsumerTask implements Runnable {
            @Override
            public void run() {
                int rpt = 0;
                while (!Thread.interrupted()) {
                    rpt++;
                    boolean done = boundedHashSet.remove("TEST-" + rpt);
                    //ThreadUtil.sleep(1);
                    System.out.println(String.format("Remains >>> %s, %s"
                        , boundedHashSet.getRemainedPermits(), done));
                }
                System.out.println("TERMINATE >>> ConsumerTask");
            }
        }


        Thread t1 = new Thread(new ProducerTask(), "ThreadOfProducer");
        Thread t2 = new Thread(new ProducerTask(), "ThreadOfConsumer");

        t1.start();
        t2.start();

        try {
            synchronized (this) {
                this.wait(3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        t1.interrupt();
        t2.interrupt();

        ThreadUtil.sleep(100); // 메시지를 보기위해서 잠깐 쉰다.

    }

    @Test
    public void addToMax() {
        int result = Integer.MAX_VALUE + 1;
        System.out.println(result);
        result = result + 1;
        System.out.println(result);

    }

}
