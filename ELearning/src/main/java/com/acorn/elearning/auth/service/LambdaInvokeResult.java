package com.acorn.elearning.auth.service;

public record LambdaInvokeResult(int statusCode, String functionError, String payload) {}
