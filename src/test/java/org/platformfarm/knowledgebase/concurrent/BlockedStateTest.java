package org.platformfarm.knowledgebase.concurrent;

import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class BlockedStateTest {

    @Test
    public void makeBlockedThreadTest () {
        Object lockObject = new Object();
        ForBlockingTest t1 = new ForBlockingTest(lockObject, "T1");
        ForBlockingTest t2 = new ForBlockingTest(lockObject, "T2");
        t1.setOtherThread(t2);
        t2.setOtherThread(t1);

        t1.start();
        ThreadUtil.sleep(300);
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class ForBlockingTest extends Thread {
        Thread otherThread;
        Object lockObject = null;

        ForBlockingTest(Object lockObject, String n) {
            this.lockObject = lockObject;
            this.setName(n);
        }

        void setOtherThread (Thread t){
            otherThread = t;
        }

        @Override
        public void run() {
            synchronized (this.lockObject) {
                int cnt = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    cnt++;
                    if (cnt > 3) {
                        break;
                    }
                    ThreadUtil.sleep(500);
                    System.out.println(String.format("Current thread name is %s, state is %s"
                        , Thread.currentThread().getName(), Thread.currentThread().getState()));
                    System.out.println(String.format("Other thread name is %s, state is %s"
                        , otherThread.getName(), otherThread.getState()));
                }
            }

        }
    }

}
