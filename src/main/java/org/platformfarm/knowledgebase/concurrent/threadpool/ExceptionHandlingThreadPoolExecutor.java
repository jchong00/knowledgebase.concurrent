package org.platformfarm.knowledgebase.concurrent.threadpool;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 이 클래스는 {@link ThreadPoolExecutor ThreadPoolExecutor}를  확장한다.
 * 확장의 목적은 스레드 내부에서 발생하는 예외를 afterExecute 내에서 처리하기 위함 이다.
 *
 * @author jchong
 * @since 2019
 *
 */
public class ExceptionHandlingThreadPoolExecutor extends ThreadPoolExecutor {

    public ExceptionHandlingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, ThreadFactory tf) {
        super(corePoolSize
            , maximumPoolSize
            , 0
            , TimeUnit.SECONDS
            , new LinkedBlockingDeque<>(), tf);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?>) { // submit 로 실행되는 thread 는 이 케이스에 해당
            Date startDate = new Date();
            try {
                ((Future<?>) r).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Date currentDate = new Date();
                // get 할 때 혹여 Lock 이 있을까 해서 시간을 측정해봤다. 0 이다.
                long gap = currentDate.getTime() - startDate.getTime();
                System.out.println("In AfterExecute ... " + gap);
                throw new RuntimeException(e);
            }
        }

        // execute 로 실행 한경우에 내부에서 오류가 발생하면 t 가 null 이 아니다. 즉 위 if 블럭에
        // 해당하지 아니한다.

        if (t != null) {
            t.printStackTrace();
        }

    }
}
