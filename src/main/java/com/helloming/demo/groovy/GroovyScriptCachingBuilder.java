package com.helloming.demo.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.Map;

public class GroovyScriptCachingBuilder {
    private GroovyShell shell = new GroovyShell();
    private Map<String, Script> scripts = new HashMap<>();

    public Script getScript(final String expression)
    {
        Script script;
        if (scripts.containsKey(expression))
        {
            script = scripts.get(expression);
        }
        else
        {
            script = shell.parse(expression);
            scripts.put(expression, script);
        }
        return script;
    }
}
