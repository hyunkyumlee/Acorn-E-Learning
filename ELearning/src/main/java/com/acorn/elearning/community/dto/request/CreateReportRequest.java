package com.acorn.elearning.community.dto.request;

import com.acorn.elearning.community.form.ReportForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotBlank @Size(max = 30) String targetType,
        @NotNull Long targetId,
        @NotBlank @Size(max = 30) String reasonCode,
        String idempotencyToken
) {
    public ReportForm toForm() {
        ReportForm form = new ReportForm();
        form.setTargetType(targetType);
        form.setTargetId(targetId);
        form.setReasonCode(reasonCode);
        form.setIdempotencyToken(idempotencyToken);
        return form;
    }
}
