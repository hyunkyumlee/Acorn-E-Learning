package com.acorn.elearning.practice.view;

import com.acorn.elearning.practice.model.WrongAnswer;
import com.acorn.elearning.security.SessionUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record WrongAnswerPageView(String title, String status, Map<String, Object> attributes) {

    public static WrongAnswerPageView stub(String title) {
        return new WrongAnswerPageView(title, "SKELETON", Map.of());
    }

    public static WrongAnswerPageView from(List<Map<String, Object>> wrongAnswers) {
        return new WrongAnswerPageView(
                "오답 목록",
                "READY",
                Map.of("wrongAnswers", wrongAnswers)
        );
    }






}
