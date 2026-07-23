package com.acorn.elearning.auth.service;

import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

public class AwsLambdaInvoker implements LambdaInvoker {

    private final LambdaClient lambdaClient;

    public AwsLambdaInvoker(LambdaClient lambdaClient) {
        this.lambdaClient = lambdaClient;
    }

    @Override
    public LambdaInvokeResult invoke(String functionName, String payload) {
        InvokeResponse response = lambdaClient.invoke(InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                .build());
        String responsePayload = response.payload() == null
                ? null
                : response.payload().asUtf8String();
        return new LambdaInvokeResult(response.statusCode(), response.functionError(), responsePayload);
    }
}
