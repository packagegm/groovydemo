package com.helloming.demo.groovy;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;

public class GroovyDemoTest extends TestCase {
    int maxCount = 50000;
    int sleepTime = 1;
    @Test
    public void testDoInGroovy() throws IOException, InterruptedException {
        int count = 0;
        while (count++ < maxCount) {
            System.out.println(">>>>>>>" + count);
            GroovyDemo.doInGroovy();
            Thread.sleep(sleepTime);
        }
    }

    @Test
    public void testDoInJava() throws InterruptedException {
        int count = 0;
        while (count++ < maxCount) {
            System.out.println(">>>>>>>" + count);
            GroovyDemo.doInJava();
            Thread.sleep(sleepTime);
        }
    }

    @Test
    public void testDoInGroovyAndClearCache() throws IOException, InterruptedException {
        int count = 0;
        while (count++ < maxCount) {
            System.out.println(">>>>>>>" + count);
            GroovyDemo.doInGroovyAndClearCache();
            Thread.sleep(sleepTime);
        }
    }
}
