package org.platformfarm.knowledgebase.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;

public class AtomicTest {

    @Test
    public void atomicIntegerTest() {

        final int REPEAT_INCREMENT = 100000;
        AtomicInteger forAtomicIntegerTest = new AtomicInteger();
        forAtomicIntegerTest.set(0);
        ExecutorService es = Executors.newFixedThreadPool(2);
        ExecutorService[] ess = new ExecutorService[] {es};

        es.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    int current = -1;
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    do {
                        // 현재 값을 읽어 비교 대상으로 해야 한다.
                        // 그래야 CAS 연산시 변경이 발생 했는지 알 수 있다.
                        current = forAtomicIntegerTest.get();
                    } while (!forAtomicIntegerTest.compareAndSet(current, current + 1));
                }
                System.out.println(String.format("CHECK POINT >>>> End of %s thread."
                    , Thread.currentThread().getName()));
                synchronized (ess) {
                    ess.notify();
                }
            }
        });

        es.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    // 단순히 읽기만 할꺼라면 현재 값을 읽는(get()) 함수를 사용할 필요가 없다.
                    forAtomicIntegerTest.incrementAndGet();
                }
                System.out.println(String.format("CHECK POINT >>>> End of %s thread."
                    , Thread.currentThread().getName()));
                synchronized (ess) {
                    ess.notify();
                }
            }
        });
        es.shutdown();
        try {
            es.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int result = forAtomicIntegerTest.get();
        System.out.println( String.format( "RESULT OF 2 Threads >>>> %s", result));
        Assert.assertEquals(REPEAT_INCREMENT * 2, result);
    }

    @Test
    public void atomicReferenceTest_Stack() {

        final int REPEAT_INCREMENT = 100000;
        ConcurrentStack<Integer> stack = new ConcurrentStack<Integer>();
        ExecutorService es = Executors.newFixedThreadPool(2);
        ExecutorService[] ess = new ExecutorService[] {es};

        es.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    stack.push(cnt);
                }

                System.out.println(String.format("CHECK POINT >>>> End of %s thread."
                    , Thread.currentThread().getName()));
                synchronized (ess) {
                    ess.notify();
                }
            }
        });

        es.execute(new Runnable() {
            @Override
            public void run() {

                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    stack.push(cnt);
                }


                System.out.println(String.format("CHECK POINT >>>> End of %s thread."
                    , Thread.currentThread().getName()));
                synchronized (ess) {
                    ess.notify();
                }
            }
        });

        es.shutdown();
        try {
            es.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int result = stack.size();
        System.out.println( String.format( "RESULT OF 2 Threads >>>> %s", result));
        Assert.assertEquals(REPEAT_INCREMENT * 2, result);
        int topOfStack = stack.pop();
        System.out.println( String.format( "TOP OF STACK VALUE >>>> %s", topOfStack));
        Assert.assertEquals(REPEAT_INCREMENT, topOfStack);
    }

    @Test
    public void atomicReferenceTest_Queue() {

        final int REPEAT_INCREMENT = 100000;
        ExecutorService es = Executors.newFixedThreadPool(2);
        ExecutorService[] ess = new ExecutorService[] {es};
        LinkedQueue<String> linkedQueue = new LinkedQueue<>();

        es.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    linkedQueue.put(Thread.currentThread().getName());
                }
            }
        });

        es.execute(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > REPEAT_INCREMENT)
                        break;
                    linkedQueue.put(Thread.currentThread().getName());
                }
            }
        });

        es.shutdown();
        try {
            es.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int result = linkedQueue.size();
        System.out.println( String.format( "RESULT OF 2 Threads >>>> %s", result));
        Assert.assertEquals(REPEAT_INCREMENT * 2, result);

    }

    @Test
    public void threadClassUsageOfRunnableWrapping() {

        // (1) 명시적으로 Runnable 을 구현한 경우
        Thread t1 = new Thread(new SimpleRunnalbe());

        // (2) 익명 객체로 Runnable 을 구현한 경우
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // thread task 구현
            }
        });

        // (2) 람다식으로 Runnable 을 구현한 경우
        Thread t3 = new Thread(()-> {
            // thread task 구현
        } );


    }


    class SimpleRunnalbe implements Runnable {

        @Override
        public void run() {

        }
    }

    class ExtendThread extends Thread {

        @Override
        public void run() {
            super.run();
            // Thread task 구현
        }
    }


} // EOF Test Class

class ConcurrentStack<E> {

    AtomicInteger size = new AtomicInteger(0);
    AtomicReference<Node<E>> head = new AtomicReference<Node<E>>();

    public void push(E item) {
        Node<E> newHead = new Node<E>(item);
        Node<E> oldHead;
        do {
            oldHead = head.get();
            newHead.next = oldHead;
        } while (!head.compareAndSet(oldHead, newHead));
        size.incrementAndGet();
    }

    public E pop() {
        Node<E> oldHead;
        Node<E> newHead;
        do {
            oldHead = head.get();
            if (oldHead == null) {
                return null;
            }
            newHead = oldHead.next;
        } while (!head.compareAndSet(oldHead, newHead));
        size.decrementAndGet();
        return oldHead.item;
    }

    public int size() {
        return size.get();
    }

    static class Node<E> {
        final E item;
        Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }
}

class LinkedQueue<E> {

    private static class Node<E> {

        final E item;
        final AtomicReference<Node<E>> next;

        Node(E item, Node<E> next) {
            this.item = item;
            this.next = new AtomicReference<Node<E>>(next);
        }
    }

    AtomicInteger size = new AtomicInteger(0);
    private AtomicReference<Node<E>> head = new AtomicReference<Node<E>>(new Node<E>(null, null));
    private AtomicReference<Node<E>> tail = head;

    boolean put(E item) {
        Node<E> newNode = new Node<E>(item, null);
        while (true) {
            Node<E> curTail = tail.get();
            Node<E> residue = curTail.next.get();
            if (curTail == tail.get()) {
                if (residue == null) /* A */ {
                    if (curTail.next.compareAndSet(null, newNode)) /* C */ {
                        tail.compareAndSet(curTail, newNode) /* D */;
                        size.incrementAndGet();
                        return true;
                    }
                } else {
                    tail.compareAndSet(curTail, residue) /* B */;
                }
            }
        }
    }

    int size() {
        return size.get();
    }
}

