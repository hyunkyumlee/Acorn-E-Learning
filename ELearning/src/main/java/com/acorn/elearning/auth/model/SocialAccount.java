package com.acorn.elearning.auth.model;

import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;
@Getter
@Setter
public class SocialAccount {
    private Long socialAccountId;
    private Long userId;
    private String provider;
    private String providerUserId;
    private String providerEmail;
    private Boolean providerEmailVerified;
    private Boolean isActive;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
}
