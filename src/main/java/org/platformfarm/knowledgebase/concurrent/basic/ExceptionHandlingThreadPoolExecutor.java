package org.platformfarm.knowledgebase.concurrent.basic;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExceptionHandlingThreadPoolExecutor extends ThreadPoolExecutor {

    public ExceptionHandlingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, ThreadFactory tf) {
        super(corePoolSize, maximumPoolSize, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), tf);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?>) {
            Date startDate = new Date();
            try {
                ((Future<?>) r).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Date currentDate = new Date();
                long gap = currentDate.getTime() - startDate.getTime();
                System.out.println("In AfterExecute ... " + gap);
                throw new RuntimeException(e);
            }
        }
        if (t != null) {
            t.printStackTrace();
        }

    }
}
