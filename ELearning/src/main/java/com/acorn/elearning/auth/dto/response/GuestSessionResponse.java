package com.acorn.elearning.auth.dto.response;

import java.util.Map;

public record GuestSessionResponse(boolean authenticated) {
    public static GuestSessionResponse guest() { return new GuestSessionResponse(false); }
}

//public record GuestSessionResponse(String status, Map<String, Object> data) {
//    public static GuestSessionResponse stub() { return new GuestSessionResponse("SKELETON", Map.of()); }
//}
