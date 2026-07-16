package com.acorn.elearning.content.view;

import java.net.URI;
import java.util.List;

public record ContentInstallResource(
        String youtubeUrl,
        List<DownloadLink> windowsDownloads,
        List<DownloadLink> macDownloads,
        List<String> windowsSteps,
        List<String> macSteps,
        List<String> windowsDetailSteps,
        List<String> macDetailSteps,
        String verifyCommand,
        String note
) {
    public String youtubeThumbnailUrl() {
        String videoId = youtubeVideoId();
        if (videoId == null || videoId.isBlank()) {
            return null;
        }
        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }

    private String youtubeVideoId() {
        if (youtubeUrl == null || youtubeUrl.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(youtubeUrl.trim());
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

    public record DownloadLink(String label, String url) {
    }
}
