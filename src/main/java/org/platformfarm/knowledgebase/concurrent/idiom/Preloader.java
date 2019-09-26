package org.platformfarm.knowledgebase.concurrent.idiom;

import java.lang.Thread.State;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.platformfarm.knowledgebase.concurrent.util.ThreadUtil;

public class Preloader {

    /**
     * future task 를 정의 한다.
     */
    private final FutureTask<ProductInfo> future =
        new FutureTask<ProductInfo>(new Callable<ProductInfo>() {
            public ProductInfo call() throws DataLoadException {
                return loadProductInfo();
            }
        });

    /**
     * 처리가 오래 걸릴것으로 예상되는 작업
     *
     * @return 결과값 Object, 멤버로 선언되어 있는 future 선언부의 템플릿 형식과 동일 해야 함
     */
    private ProductInfo loadProductInfo() {
        ThreadUtil.sleep(5000);
        return new ProductInfo();
    }

    /**
     * 위에서 정의한 future 로 thread 를 정의
     */
    private final Thread thread = new Thread(future);

    /**
     * get()이전에 이 함수를 미리 불러놓으면 처리시간을 줄일 수 있다.
     */
    public void start() { thread.start(); }

    public ProductInfo get() {
        try {

            if(thread.getState() == State.NEW) {
                thread.start();
            }
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            Throwable cause = e.getCause();
            if (cause instanceof DataLoadException)
                throw (DataLoadException) cause;
            else
                throw launderThrowable(cause);
        }
    }

    private RuntimeException launderThrowable(Throwable cause) {

        if (cause instanceof RuntimeException)
            return (RuntimeException)cause;
        else if (cause instanceof Error)
            throw (Error) cause;
        else
            throw new IllegalStateException("Not Unchecked", cause);
    }

    public static class ProductInfo {

    }

    private static class DataLoadException extends RuntimeException {

    }
}
