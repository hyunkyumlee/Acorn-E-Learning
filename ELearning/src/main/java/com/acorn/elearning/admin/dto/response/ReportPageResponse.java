package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReportPageResponse(
        List<ReportItem> reports
){
    public record ReportItem(
            Long reportId,
            String reporterNickname,
            String targetType,
            Long targetId,
            String targetSummary,
            String reasonCode,
            String status,
            LocalDateTime createdAt
    ){

        public String targetTypeLabel() {
            if ("POST".equals(targetType)) {
                return "게시글";
            }
            if ("COMMENT".equals(targetType)) {
                return "댓글";
            }
            return hasText(targetType) ? targetType : "-";
        }

        public String reasonLabel() {
            if ("SPAM".equals(reasonCode)) {
                return "스팸";
            }
            if ("ABUSE".equals(reasonCode)) {
                return "욕설 및 비방";
            }
            if ("AD".equals(reasonCode)) {
                return "광고성 게시물";
            }
            if ("PRIVACY".equals(reasonCode)) {
                return "개인정보 노출";
            }
            return hasText(reasonCode) ? reasonCode : "-";
        }

        public String statusLabel() {
            if ("PENDING".equals(status)) {
                return "대기";
            }
            if ("IN_PROGRESS".equals(status)) {
                return "검토 중";
            }
            if ("RESOLVED".equals(status)) {
                return "처리 완료";
            }
            if ("REJECTED".equals(status)) {
                return "반려";
            }
            return hasText(status) ? status : "-";
        }

        public String statusClass() {
            if ("RESOLVED".equals(status)) {
                return " is-done";
            }
            if ("IN_PROGRESS".equals(status)) {
                return " is-reviewing";
            }
            return " is-pending";
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
