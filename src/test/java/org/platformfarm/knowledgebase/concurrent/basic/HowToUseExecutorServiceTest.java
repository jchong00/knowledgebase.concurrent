package org.platformfarm.knowledgebase.concurrent.basic;

import static org.junit.Assert.*;

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
}