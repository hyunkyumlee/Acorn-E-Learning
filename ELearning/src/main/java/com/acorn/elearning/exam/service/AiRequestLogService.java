package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.common.ai.ChatGptRequest;
import com.acorn.elearning.common.ai.ChatGptResponse;
import com.acorn.elearning.exam.mapper.AiRequestLogMapper;
import com.acorn.elearning.exam.model.AiRequestLog;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiRequestLogService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";

    private final AiRequestLogMapper aiRequestLogMapper;
    private final ObjectMapper objectMapper;

    public AiRequestLogService(AiRequestLogMapper aiRequestLogMapper, ObjectMapper objectMapper) {
        this.aiRequestLogMapper = aiRequestLogMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiRequestLog start(String targetType, Long targetId, String requestType, ChatGptRequest request) {
        AiRequestLog log = new AiRequestLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setRequestType(requestType);
        log.setStatus(STATUS_PENDING);
        log.setRetryNo(0);
        log.setRequestPayload(toJson(request));
        aiRequestLogMapper.insert(log);
        return log;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(AiRequestLog log, ChatGptResponse response) {
        log.setStatus(STATUS_SUCCESS);
        log.setResponsePayload(toJson(response));
        log.setErrorCode(null);
        log.setErrorMessage(null);
        aiRequestLogMapper.update(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(AiRequestLog log, Exception exception) {
        markFailed(log, null, exception);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failed(AiRequestLog log, ChatGptResponse response, Exception exception) {
        markFailed(log, response, exception);
    }

    private void markFailed(AiRequestLog log, ChatGptResponse response, Exception exception) {
        log.setStatus(STATUS_FAILED);
        if (response != null) {
            log.setResponsePayload(toJson(response));
        }
        log.setErrorCode(exception instanceof BusinessException businessException
                ? businessException.errorCode().code()
                : ErrorCode.COMMON_INTERNAL_ERROR.code());
        log.setErrorMessage(exception.getMessage());
        aiRequestLogMapper.update(log);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "AI 요청 로그를 저장할 수 없습니다.");
        }
    }
}
