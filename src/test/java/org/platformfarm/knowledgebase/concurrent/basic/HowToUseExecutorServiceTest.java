package org.platformfarm.knowledgebase.concurrent.basic;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class HowToUseExecutorServiceTest {

    @Test
    public void newFixedThreadPoolExam() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        testTarget.newFixedThreadPoolExam();

    }

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

    @Test
    public void executorSubmitUsingCustomThreadPoolExecutor() {
        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingSubmit2();
        Assert.assertEquals("", last);
    }


    /**
     * 예외가 발생하면 해당 Thread 는 깨진다.
     *
     * 테스트는 execute 가 예외를 JVM 에 파생을 시키기 때문에 Feedback 이 전달되어 함수의 반환
     * 값인 예외를 던지 마지막 Thread 이름이 빈 값이 아닌지 단정한다.
     */
    @Test
    public void executorExecute() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingExecute();
        Assert.assertNotEquals("", last);

    }


}