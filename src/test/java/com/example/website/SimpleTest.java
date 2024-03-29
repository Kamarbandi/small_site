package com.example.website;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {
    @Test
    public void test() {
        int x = 5;
        int y = 10;

        Assert.assertEquals(15, x + y);
        Assert.assertEquals(50, x * y);
    }

    @Test(expected = ArithmeticException.class)
    public void error() {
        int i = 0;
        int i1 = 1 / i;
    }
}
