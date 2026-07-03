package com.acorn.elearning.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record SignupRequest(@NotBlank @Email String email,
                            @NotBlank @Size(min = 8, max = 72) String password,
                            @NotBlank @Size(min = 2, max = 50) String nickname,
                            Long primarySubjectId, String LearningGoal) { }

//public record SignupRequest(@NotBlank String requestId, Map<String, Object> payload) {}
