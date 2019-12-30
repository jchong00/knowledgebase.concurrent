package org.platformfarm.knowledgebase.concurrent.basic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public interface ExExecutors {

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ExceptionHandlingThreadPoolExecutor(nThreads, nThreads, threadFactory);
    }
}
