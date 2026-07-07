package com.acorn.elearning.learning.view;

/**
 * 로드맵 난이도 레벨 탭 표시용.
 * @param code       레벨 코드(BRONZE/SILVER/GOLD)
 * @param accessible 선택 가능한(해금된) 레벨인지 — false면 잠금 표시
 * @param selected   현재 표시 중인 레벨인지
 */
public record RoadmapLevelTab(String code, boolean accessible, boolean selected) {}
