package com.acorn.elearning.admin.dto.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminUserManageRowResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String subjectName;
    private String gradeCode;
    private Integer totalScore;
    private String role;
    private LocalDateTime createdAt;
    private String status;

    private Integer progressCount;
    private Integer completedCount;
    private Double averageProgressRate;

}
