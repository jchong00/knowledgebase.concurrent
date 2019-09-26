package org.platformfarm.knowledgebase.concurrent;

import java.lang.management.ThreadInfo;
import java.nio.channels.CompletionHandler;
import org.junit.Assert;
import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class ThreadGroupTest {

    @Test
    public void threadGroupEnumerateTest () {

        class TaskOfTest implements Runnable {
            @Override
            public void run() {
                ThreadUtil.sleep(3000);
            }
        }

        ThreadGroup tg = new ThreadGroup("MyThreadGroup");
        tg.setDaemon(true);
        ThreadGroup tgs = new ThreadGroup(tg, "MyThreadSubGroup");
        tgs.setDaemon(true);

        Thread t1 = new Thread(tg, new TaskOfTest());
        t1.start();
        Thread t2 = new Thread(tgs, new TaskOfTest());
        t2.start();

        Thread[] threads = new Thread[tg.activeGroupCount()];
        tg.enumerate(threads);

        Assert.assertEquals(1, threads.length);



    }


}
