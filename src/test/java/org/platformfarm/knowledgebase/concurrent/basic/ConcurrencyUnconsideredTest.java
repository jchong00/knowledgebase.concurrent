package org.platformfarm.knowledgebase.concurrent.basic;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class ConcurrencyUnconsideredTest {

    @Test
    public void twoThreadShareVariableIssue() {
        ConcurrencyUnconsidered testTarget = new ConcurrencyUnconsidered();
        int result = testTarget.twoThreadShareVariableIssue();
        Assert.assertNotEquals(20000, result);
        System.out.println("Unconsidered concurrency result: " + result);
    }
}
