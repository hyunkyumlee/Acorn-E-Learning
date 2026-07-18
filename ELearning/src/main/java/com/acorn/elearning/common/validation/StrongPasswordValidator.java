package com.acorn.elearning.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 공백/누락은 @NotBlank가 따로 처리하므로 여기서는 통과시킴(중복 에러 메시지 방지)
        if (value == null || value.isBlank()) return true;
        return PasswordPolicy.isStrong(value);
    }
}
