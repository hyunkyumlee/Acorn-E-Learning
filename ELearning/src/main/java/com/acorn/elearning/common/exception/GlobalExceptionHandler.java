package com.acorn.elearning.common.exception;

import com.acorn.elearning.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String VALIDATION_DETAIL = "요청 본문 검증에 실패했습니다.";

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(exception.getMessage(), errorCode.code(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return validationResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        return validationResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        ErrorCode errorCode = ErrorCode.COMMON_VALIDATION_FAILED;
        List<ApiResponse.FieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new ApiResponse.FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();

        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.message(), errorCode.code(), VALIDATION_DETAIL, fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException() {
        ErrorCode errorCode = ErrorCode.COMMON_VALIDATION_FAILED;
        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.message(), errorCode.code(), "요청 본문을 읽을 수 없습니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    Object handleNoResourceFoundException(HttpServletRequest request) {
        if (acceptsHtml(request)) {
            return errorPage("error/404", HttpStatus.NOT_FOUND);
        }

        ErrorCode errorCode = ErrorCode.COMMON_NOT_FOUND;
        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.message(), errorCode.code(), errorCode.message()));
    }

    @ExceptionHandler(Exception.class)
    Object handleException(Exception exception, HttpServletRequest request) {
        if (acceptsHtml(request)) {
            return errorPage("error/500", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ErrorCode errorCode = ErrorCode.COMMON_INTERNAL_ERROR;
        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.message(), errorCode.code(), "처리 중 알 수 없는 오류가 발생했습니다."));
    }

    private ResponseEntity<ApiResponse<Void>> validationResponse(List<FieldError> errors) {
        ErrorCode errorCode = ErrorCode.COMMON_VALIDATION_FAILED;
        List<ApiResponse.FieldError> fieldErrors = errors.stream()
                .map(this::toFieldError)
                .toList();

        return ResponseEntity
                .status(errorCode.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.fail(errorCode.message(), errorCode.code(), VALIDATION_DETAIL, fieldErrors));
    }

    private ApiResponse.FieldError toFieldError(FieldError error) {
        String message = error.isBindingFailure()
                ? "입력값 형식이 올바르지 않습니다."
                : error.getDefaultMessage();
        return new ApiResponse.FieldError(error.getField(), message);
    }

    private boolean acceptsHtml(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept == null || accept.isBlank()) {
            return false;
        }

        try {
            List<MediaType> acceptedMediaTypes = new ArrayList<>(MediaType.parseMediaTypes(accept));
            acceptedMediaTypes.removeIf(mediaType -> mediaType.getQualityValue() <= 0);
            acceptedMediaTypes.sort(Comparator.comparingDouble(MediaType::getQualityValue).reversed());

            return !acceptedMediaTypes.isEmpty() && isTextHtml(acceptedMediaTypes.get(0));
        } catch (InvalidMediaTypeException exception) {
            return false;
        }
    }

    private boolean isTextHtml(MediaType mediaType) {
        return MediaType.TEXT_HTML.getType().equalsIgnoreCase(mediaType.getType())
                && MediaType.TEXT_HTML.getSubtype().equalsIgnoreCase(mediaType.getSubtype());
    }

    private ModelAndView errorPage(String viewName, HttpStatus status) {
        ModelAndView errorPage = new ModelAndView(viewName);
        errorPage.setStatus(status);
        return errorPage;
    }
}
