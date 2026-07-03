package com.acorn.elearning.auth.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCredential {
    private Long credentialId;
    private Long userId;
    private String passwordHash;
    private LocalDateTime passwordUpdatedAt;
    private Integer failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
