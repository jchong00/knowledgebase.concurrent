package org.platformfarm.knowledgebase.concurrent.threadpool;

import org.junit.Assert;
import org.junit.Test;
import org.platformfarm.knowledgebase.concurrent.threadpool.HowToUseExecutorService;

public class HowToUseExecutorServiceTest {

    /**
     *  고정된 크기의 thread pool 을 이용하는 테스트 케이스, Submit 를 사용한다.
     *
     */
    @Test
    public void newFixedThreadPoolExamBySubmit() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        testTarget.newFixedThreadPoolExamBySubmit();

    }

    /**
     *  고정된 크기의 thread pool 을 이용하는 테스트 케이스, Submit 를 사용한다.
     *
     */
    @Test
    public void newFixedThreadPoolExamBySubmitAndGetByFuture() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        int test = testTarget.newFixedThreadPoolExamBySubmitAndFuture();
        Assert.assertEquals(0, test);

    }

    /**
     *  고정된 크기의 thread pool 을 이용하는 테스트 케이스, Execute 를 사용한다.
     *
     */
    @Test
    public void newFixedThreadPoolExamByExecute() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        testTarget.newFixedThreadPoolExamByExecute();

    }

    /**
     *  요청된 작업의 수 만큼 thread 수를 늘렸다가 작업 요청이 없으면 줄이는 thread pool 을 이용하는
     *  테스트 케이스
     */
    @Test
    public void newCachedThreadPoolExam() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        testTarget.newCachedThreadPoolExam();
    }

    /**
     *  submit 은 예외를 먹는다. 결과를 돌려 받을 수 있는 방법을 제공한다. 스레드를 재사용한다.
     *
     *  테스트는 submit 가 예외를 먹어서 전역 예외처리기에 아무런 Feedback 도 하지 않아 함수의
     *  반환 값인 예외를 던진 마지막 Thread 이름이 빈 값인지를 단정한다.
     */
    @Test
    public void executorSubmit() {
        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingSubmit();
        Assert.assertEquals("", last);
    }

    /**
     *  원래는 이 테스트 함수도 대상 함수의 내부에서 thread 를 다룰때 submit 을 사용하므로 예외가
     *  발생하면 먹어 치워야 한다. 그러나 ThreadPoolExecutor 를 확장하여 오류가 발생하면 Runtime -
     *  Exception 을 발생하게 했다. 따라서 마치 execute 로 스레드를 실행한 것 같은 효과를 갖는다.
     */
    @Test
    public void executorSubmitUsingCustomThreadPoolExecutor() {
        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingSubmit2();
        Assert.assertNotEquals("", last);
    }

    /**
     *
     */
    @Test
    public void executorSubmitUsingGetExceptionCheck() {
        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        testTarget.throwsExceptionRunnableTaskUsingSubmit3();
    }


    /**
     * 예외가 발생하면 해당 Thread 는 깨진다.
     *
     * 테스트는 execute 가 예외를 JVM 에 전파 시키기 때문에 Feedback 이 전달되어 함수의 반환
     * 값인 예외를 던진 마지막 Thread 이름이 빈 값이 아닌지 단정한다.
     */
    @Test
    public void executorExecute() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingExecute();
        Assert.assertNotEquals("", last);

    }

    /**
     * 예외가 발생하면 해당 Thread 는 깨진다.
     *
     * 테스트는 execute 가 예외를 JVM 에 파생을 시키기 때문에 Feedback 이 전달되어 함수의 반환
     * 값인 예외를 던진 마지막 Thread 이름이 빈 값이 아닌지 단정한다.
     *
     * 상기 함수와 동일한 처리를 하고 동일한 결과를 얻는다. 이 테스트는 Custom 된 ThreadPoolExecutor
     * 를 사용했을 때 afterExecute override 에서 예외처리가 어떻게 되는지 확인하기 위함이다.
     *
     */
    @Test
    public void executorExecuteUsingCustomThreadPoolExecutor() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingExecute2();
        Assert.assertNotEquals("", last);

    }
}