package com.acorn.elearning.community.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostForm {
    public static final int CONTENT_MAX_LENGTH = 10_000;

    private Long draftPostId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = CONTENT_MAX_LENGTH, message = "본문은 10,000자 이하로 입력해주세요.")
    private String content;

    @NotNull
    private Long subjectId;

    @NotBlank
    @Size(max = 30)
    private String boardType = "FREE";

    private String idempotencyToken;
    private List<MultipartFile> files = new ArrayList<>();
    private List<Long> deleteAttachmentIds = new ArrayList<>();
}
