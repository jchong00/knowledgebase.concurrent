package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * {@link ExceptionHandlingThreadPoolExecutor} 등 확장된 Executor를 생성하는 팩토리 class 정의
 *
 */
public interface CustomExecutors {

    static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ExceptionHandlingThreadPoolExecutor(nThreads, nThreads, threadFactory);
    }
}
