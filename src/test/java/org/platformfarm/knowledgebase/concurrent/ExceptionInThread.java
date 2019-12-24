package org.platformfarm.knowledgebase.concurrent;

import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class ExceptionInThread {

    @Test
    public void throwUnhandledExceptionInThread() {
        Thread t = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                Object nullObj = null;
                String s = nullObj.toString();
            }

        });

        t.start();
        ThreadUtil.sleep(1000);
        String st = t.getState().toString();
        System.out.println(st);
        t.interrupt();
        //ThreadUtil.sleep(2000);

    }


    public void alwaysThrowsNpe() {
        Object obj = null;
        try {
            obj.toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
