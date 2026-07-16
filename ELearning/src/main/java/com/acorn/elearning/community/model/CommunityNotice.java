package com.acorn.elearning.community.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityNotice {
    private Long noticeId;
    private String title;
    private String content;
    private String writerNickname;
    private LocalDateTime publishedAt;
}
