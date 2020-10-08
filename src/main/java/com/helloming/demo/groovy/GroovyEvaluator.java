package com.helloming.demo.groovy;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroovyEvaluator {
    private static GroovyScriptCachingBuilder groovyScriptCachingBuilder = new GroovyScriptCachingBuilder();
    private Map<String, Object> variables = new HashMap<>();

    public GroovyEvaluator() {
        this(Collections.<String, Object>emptyMap());
    }

    public GroovyEvaluator(final Map<String, Object> contextVariables) {
        variables.putAll(contextVariables);
    }

    public void setVariables(final Map<String, Object> answers) {
        variables.putAll(answers);
    }

    public void setVariable(final String name, final Object value) {
        variables.put(name, value);
    }

    public Object evaluateExpression(String expression) {
        final Binding binding = new Binding();
        for (Map.Entry<String, Object> varEntry : variables.entrySet()) {
            binding.setProperty(varEntry.getKey(), varEntry.getValue());
        }
        Script script = groovyScriptCachingBuilder.getScript(expression);
        synchronized (script) {
            script.setBinding(binding);
            return script.run();
        }
    }

    public Object evaluateMethod(String expression, String method, Object args) {
        Script script = groovyScriptCachingBuilder.getScript(expression);
        if (script != null) {
            synchronized (script) {
                return script.invokeMethod(method, args);
            }
        } else {
            return null;
        }
    }
}
