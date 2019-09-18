package org.platformfarm.knowledgebase.concurrent;

import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;import java.time.Clock;
import org.junit.Test;

public class JoinTest {

    @Test
    public void threadStartAndJoinTest() {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Clock clock = Clock.systemUTC();
                System.out.println(String.format("Before sleep >>> %s", clock.instant()));
                ThreadUtil.sleep(3000);
                System.out.println(String.format("After sleep >>> %s", clock.instant()));
            }
        });

        t1.start();

        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
