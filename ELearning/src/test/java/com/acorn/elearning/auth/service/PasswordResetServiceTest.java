package com.acorn.elearning.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.auth.mapper.PasswordResetTokenMapper;
import com.acorn.elearning.auth.mapper.UserCredentialMapper;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.user.mapper.UserMapper;
import java.lang.reflect.Proxy;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PasswordResetServiceTest {

    @Test
    void requestReset_rejects_unregistered_email() {
        // Given
        PasswordResetService service = new PasswordResetService(
                mapper(PasswordResetTokenMapper.class, (method, args) -> {
                    if (method.getName().equals("deleteExpired")) {
                        return 0;
                    }
                    throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                }),
                mapper(UserCredentialMapper.class, (method, args) -> {
                    if (method.getName().equals("findByLoginEmail")) {
                        return Optional.empty();
                    }
                    throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                }),
                mapper(UserMapper.class, (method, args) -> {
                    if (method.getName().equals("findByEmail")) {
                        return Optional.empty();
                    }
                    throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                }),
                null,
                null);

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.requestReset("not-registered@example.com"));

        // Then
        assertEquals(ErrorCode.AUTH_USER_NOT_FOUND, exception.errorCode());
        assertEquals("가입되지 않은 이메일입니다.", exception.getMessage());
    }

    private static <T> T mapper(Class<T> type, MapperInvocation invocation) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (target, method, args) -> invocation.invoke(method, args));
        return type.cast(proxy);
    }

    @FunctionalInterface
    private interface MapperInvocation {
        Object invoke(java.lang.reflect.Method method, Object[] args);
    }
}
