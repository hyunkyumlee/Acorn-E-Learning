package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityStatusForm {

    @Pattern(
            regexp = "ACTIVE|HIDDEN|DELETED",
            message = "허용되지 않은 상태 값입니다."
    )
    private String status;
}
