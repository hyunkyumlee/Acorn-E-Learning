package com.acorn.elearning.common.response;

// API가 성공했지만, data에 추가로 줄 정보나 객체가 없을 때 사용하는 record
// REST AUTH-003 등 — ApiResponse의 data가 비어 있는 성공 응답용 record

public record VoidResponse() {
    public static final VoidResponse INSTANCE = new VoidResponse();
}
