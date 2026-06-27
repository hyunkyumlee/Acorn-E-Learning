package com.acorn.elearning.common.ai;

import java.util.Map;

public record ChatGptRequest(String purpose, String promptVersion, Map<String, Object> payload) {}
