package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public interface ExExecutors {

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ExceptionHandlingThreadPoolExecutor(nThreads, nThreads, threadFactory);
    }
}
