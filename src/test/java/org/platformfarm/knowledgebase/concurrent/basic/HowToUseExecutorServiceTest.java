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
     */
    @Test
    public void executorSubmit() {
        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingSubmit();
        Assert.assertEquals("", last);
    }

    /**
     * 예외가 발생하면 해당 Thread 는 깨진다.
     *
     */
    @Test
    public void executorExecute() {

        HowToUseExecutorService testTarget = new HowToUseExecutorService();
        String last = testTarget.throwsExceptionRunnableTaskUsingExecute();
        Assert.assertNotEquals("", last);

    }


}