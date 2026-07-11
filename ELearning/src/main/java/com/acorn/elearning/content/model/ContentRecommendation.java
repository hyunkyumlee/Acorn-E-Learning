package com.acorn.elearning.content.model;

import java.net.URI;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentRecommendation {
    private Long contentId;
    private Long subjectId;
    private String title;
    private String url;
    private String contentType;
    private String recommendationSlot;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String youtubeThumbnailUrl() {
        String videoId = youtubeVideoId();
        if (videoId == null || videoId.isBlank()) {
            return null;
        }
        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }

    private String youtubeVideoId() {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            if (host == null) {
                return null;
            }
            String normalizedHost = host.toLowerCase();
            if (normalizedHost.endsWith("youtu.be")) {
                return firstPathSegment(uri.getPath());
            }
            if (!normalizedHost.contains("youtube.com")) {
                return null;
            }
            String path = uri.getPath();
            if (path != null && (path.startsWith("/embed/") || path.startsWith("/shorts/"))) {
                return path.substring(path.lastIndexOf('/') + 1);
            }
            return queryParam(uri.getRawQuery(), "v");
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String firstPathSegment(String path) {
        if (path == null || path.length() <= 1) {
            return null;
        }
        int nextSlash = path.indexOf('/', 1);
        return nextSlash > 0 ? path.substring(1, nextSlash) : path.substring(1);
    }

    private String queryParam(String query, String name) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            if (name.equals(key)) {
                return separator >= 0 ? pair.substring(separator + 1) : "";
            }
        }
        return null;
    }
}
