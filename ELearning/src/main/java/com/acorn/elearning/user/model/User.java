    package com.acorn.elearning.user.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class User {
        private Long userId;
private String email;
private String nickname;
private String role;
private String status;
private String profileImageUrl;
private LocalDateTime lastLoginAt;
private LocalDateTime withdrawnAt;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
