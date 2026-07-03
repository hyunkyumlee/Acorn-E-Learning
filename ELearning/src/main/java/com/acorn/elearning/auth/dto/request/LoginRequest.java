package com.acorn.elearning.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;



//
public record LoginRequest(@NotBlank @Email String email, @NotBlank String password, boolean rememberMe) {}
//public record LoginRequest(@NotBlank String requestId, Map<String, Object> payload) {}
