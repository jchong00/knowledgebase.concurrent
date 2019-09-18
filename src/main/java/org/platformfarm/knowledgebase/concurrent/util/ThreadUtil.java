package org.platformfarm.knowledgebase.concurrent.util;

import java.util.Optional;
import java.util.Set;

public class ThreadUtil {

    private final static Object lockOfGetName = new Object();

    public static void sleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean aliveThread(String threadName) {
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        Optional<Thread> foundThread
            = threads.stream().filter(thread -> thread.getName().equals(threadName)).findFirst();

        if (foundThread.isPresent()) {
            return true;
        }
        return false;
    }

    public static String getNextThreadName(String baseName) {
        synchronized (lockOfGetName) {

            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            long cnt = threads.stream().filter(thread -> thread.getName().startsWith(baseName))
                .count();
            return String.format("%s-%d", baseName, cnt);
        }
    }

}
