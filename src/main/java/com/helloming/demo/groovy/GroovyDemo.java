package com.helloming.demo.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.Script;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.GroovyClassValue;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GroovyDemo {
    public static void doInGroovy() throws IOException {
        GroovyShell groovyShell = new GroovyShell();
        Random random = new Random();
        List groovyParams = new ArrayList();
        groovyParams.add(random.nextInt());
        String groovyRoot = Thread.currentThread().getContextClassLoader().getResource("groovy").getPath();
        System.out.println(groovyRoot);
        Script script = groovyShell.parse(new File( groovyRoot + File.separator + "CalculateRandomG.groovy"));
        String result = (String) (script != null ? script.invokeMethod("calculate", groovyParams) : null);
    }

    /**
     * from https://stackoverflow.com/questions/41465834/repeated-use-of-groovy-shells-results-in-permgen-space-full/41473210#41473210
     */
    public static void doInGroovyAndClearCache() throws IOException {
        GroovyShell groovyShell = new GroovyShell();
        Random random = new Random();
        List groovyParams = new ArrayList();
        groovyParams.add(random.nextInt());
        String groovyRoot = Thread.currentThread().getContextClassLoader().getResource("groovy").getPath();
        System.out.println(groovyRoot);
        Script script = groovyShell.parse(new File( groovyRoot + File.separator + "CalculateRandomG.groovy"));
        String result = (String) (script != null ? script.invokeMethod("calculate", groovyParams) : null);
//        clearCache(script, groovyShell); // 测试后无效
        clearAllClassInfo(script.getClass());
    }

    /**
     * from https://stackoverflow.com/questions/36407119/groovyshell-in-java8-memory-leak-duplicated-classes-src-code-load-test-pr
     * @throws IOException
     */
    public static void doInGroovyCached() throws IOException {
        Random random = new Random();
        List groovyParams = new ArrayList();
        groovyParams.add(random.nextInt());
        GroovyEvaluator groovyEvaluator = new GroovyEvaluator();
        String groovyRoot = Thread.currentThread().getContextClassLoader().getResource("groovy").getPath();
        String result = (String) groovyEvaluator.evaluateMethod(ResourceGroovyMethods.getText(
                new File( groovyRoot + File.separator + "CalculateRandomG.groovy")), "calculate", groovyParams);
    }

    public static void doInJava() {
        Random random = new Random();
        int randomInt = random.nextInt();
        String result = new CalculateRandomJ().calculate(randomInt);
    }

    private static void clearAllClassInfo(Class<?> type) {
        try {
            Field globalClassValue = ClassInfo.class.getDeclaredField("globalClassValue");
            globalClassValue.setAccessible(true);
            GroovyClassValue classValueBean = (GroovyClassValue) globalClassValue.get(null);
            classValueBean.remove(type);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 无效
     * @param script
     * @param groovyShell
     */
    private static void clearCache(Script script, GroovyShell groovyShell) {
        GroovySystem.getMetaClassRegistry().removeMetaClass(script.getClass());
        groovyShell.getClassLoader().clearCache();
    }
}
