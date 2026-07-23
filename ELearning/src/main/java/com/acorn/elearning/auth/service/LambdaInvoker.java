package com.acorn.elearning.auth.service;

@FunctionalInterface
public interface LambdaInvoker {

    LambdaInvokeResult invoke(String functionName, String payload);
}
