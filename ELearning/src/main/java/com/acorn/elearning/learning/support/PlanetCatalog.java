package com.acorn.elearning.learning.support;

import java.util.List;

/**
 * 로드맵 행성 표시 메타(아트 파일명 · 행성 이름 · 테마 스토리) 단일 소스.
 *
 * 행성 15종을 레벨당 5개씩 고정 배치한다: BRONZE=0~4, SILVER=5~9, GOLD=10~14.
 * planet_no(1~5)는 (subject, level)당 반복되므로 (레벨, planet_no)로 15종 중 하나를 고른다.
 * 스토리의 {학습주제} 토큰은 노드 제목에서 과목/레벨 라벨을 걷어낸 주제로 치환한다.
 *
 * 화면(main.html 로드맵 카드 / lesson-list 히어로)이 같은 데이터를 쓰도록 여기 한 곳에 둔다.
 */
public final class PlanetCatalog {

    /** 화면에 그대로 바인딩되는 행성 표시 값(아트 파일 basename · 이름 · 치환 완료 스토리). */
    public record PlanetView(String file, String name, String story) {}

    private record Entry(String file, String name, String flavor, String theme) {}

    private static final String TOPIC = "{학습주제}";
    private static final int PER_LEVEL = 5;
    private static final List<String> LEVELS = List.of("BRONZE", "SILVER", "GOLD");

    /** 스토리 원문 = Planet_Concept 기준. flavor(분위기) + theme(학습주제 반영) 두 문장. */
    private static final Entry[] ENTRIES = {
        // ── BRONZE ─────────────────────────────────────────────────────────
        new Entry("00_Fruition_Planet", "Fruition Planet",
            "달콤한 결실이 탐스럽게 익어가는 Fruition Planet입니다.",
            TOPIC + "의 기본 개념이 잘 익은 과실처럼 하나씩 맺혀 있는 행성입니다."),
        new Entry("01_Blue_Terra_Planet", "Blue Terra Planet",
            "푸른 숨결과 생명감이 흐르는 Blue Terra Planet입니다.",
            TOPIC + "의 큰 흐름이 생명력 있는 대지와 바다처럼 넓게 이어지는 행성입니다."),
        new Entry("02_Celestia_Cove_Planet", "Celestia Cove Planet",
            "햇살에 반짝이는 푸른 만과 느긋한 해안의 바람이 감도는 Celestia Cove Planet입니다.",
            TOPIC + "의 개념들이 잔물결처럼 끊기지 않고 이어지는 행성입니다."),
        new Entry("03_Gracier_Planet", "Gracier Planet",
            "차갑고 단단한 빛이 고요하게 쌓인 Gracier Planet입니다.",
            TOPIC + "의 핵심 개념이 빙하층처럼 또렷하고 견고하게 자리 잡는 행성입니다."),
        new Entry("04_Sunstone_Mesa_Planet", "Sunstone Mesa Planet",
            "붉은 협곡 사이로 따뜻한 빛이 스며드는 Sunstone Mesa Planet입니다.",
            TOPIC + "의 중요한 개념들을 사막 속 오아시스처럼 찾아 나서는 행성입니다."),
        // ── SILVER ─────────────────────────────────────────────────────────
        new Entry("05_Bloom_Halo_Planet", "Bloom Halo Planet",
            "꽃내음이 가득히 퍼지는 Bloom Halo Planet입니다.",
            TOPIC + "에 대한 지식이 꽃잎처럼 겹겹이 피어나는 행성입니다."),
        new Entry("06_DreamFair_Planet", "DreamFair Planet",
            "달콤한 공기와 함께 두둥실 떠다니는 DreamFair Planet입니다.",
            TOPIC + "의 작은 개념들이 가볍게 떠오르며 즐겁게 모여드는 행성입니다."),
        new Entry("07_Hannabi_Planet", "Hannabi Planet",
            "한여름 밤의 빛이 아름답게 번지는 Hannabi Planet입니다.",
            TOPIC + "의 개념들이 이어지는 순간 선명한 빛처럼 펼쳐지는 행성입니다."),
        new Entry("08_MossWhisper_Planet", "MossWhisper Planet",
            "조용한 숲의 비밀이 자라나는 MossWhisper Planet입니다.",
            TOPIC + "의 패턴과 예제가 숲속의 흔적처럼 곳곳에서 발견되는 행성입니다."),
        new Entry("09_Chrome_Nexus_Planet", "Chrome Nexus Planet",
            "차갑게 빛나는 금속 조각들이 맞물린 Chrome Nexus Planet입니다.",
            TOPIC + "의 구조와 원리가 맞물려 정교한 기계처럼 작동하는 행성입니다."),
        // ── GOLD ───────────────────────────────────────────────────────────
        new Entry("10_Lotus_Drift_Planet", "Lotus Drift Planet",
            "잔잔한 물길 위로 작은 모험이 시작되는 Lotus Drift Planet입니다.",
            TOPIC + "의 흐름을 조심스럽게 띄워 보내며 차근차근 따라가는 행성입니다."),
        new Entry("11_Opal_BonBon_Planet", "Opal BonBon Planet",
            "반짝이는 달콤함이 감도는 Opal BonBon Planet입니다.",
            TOPIC + "의 개념들이 알록달록한 조각처럼 모여 하나의 맛을 완성하는 행성입니다."),
        new Entry("12_MoonRabbit_Planet", "MoonRabbit Planet",
            "포근한 온기와 고요한 기운이 감도는 MoonRabbit Planet입니다.",
            TOPIC + "의 개념들이 초승달에서 보름달로 차오르듯 선명해지는 행성입니다."),
        new Entry("13_Mermaid_Lagoon_Planet", "Mermaid Lagoon Planet",
            "달빛을 머금은 아름다운 바다의 빛을 간직한 Mermaid Lagoon Planet입니다.",
            TOPIC + "의 흐름을 따라가다 보면 숨겨진 진주처럼 핵심이 선명하게 드러나는 행성입니다."),
        new Entry("14_EmberFlow_Planet", "EmberFlow Planet",
            "뜨거운 열기와 꺼지지 않는 불꽃이 흐르는 EmberFlow Planet입니다.",
            TOPIC + "의 개념이 달아오른 열기처럼 이어지고, 흐르는 용암처럼 선명해지는 행성입니다."),
    };

    /** 노드 제목 앞에 흔히 붙는 과목/레벨 라벨(주제 정제 시 제거 대상). */
    private static final String[] LABEL_PREFIXES = {
        "JAVA", "Java", "PYTHON", "Python", "WEB", "Web", "SQL", "Sql",
        "BRONZE", "Bronze", "브론즈", "SILVER", "Silver", "실버", "GOLD", "Gold", "골드"
    };

    private PlanetCatalog() {}

    /**
     * (레벨, planet_no 1~5) → 그 자리에 놓일 행성 아트/이름/스토리.
     * @param rawTitle    노드 제목(예: "Java 변수와 자료형 행성") — 스토리 {학습주제} 치환에 사용
     * @param subjectCode 과목 코드(주제 정제 시 앞머리 제거) — null 허용
     */
    public static PlanetView resolve(String levelCode, Integer planetNo, String rawTitle, String subjectCode) {
        int levelIdx = LEVELS.indexOf(levelCode == null ? "" : levelCode.toUpperCase());
        if (levelIdx < 0) {
            levelIdx = 0;
        }
        int no = (planetNo == null || planetNo < 1) ? 1 : planetNo;
        int idx = levelIdx * PER_LEVEL + ((no - 1) % PER_LEVEL);
        Entry e = ENTRIES[idx];
        String topic = cleanTopic(rawTitle, subjectCode);
        String story = e.flavor() + " " + e.theme().replace(TOPIC, topic);
        return new PlanetView(e.file(), e.name(), story);
    }

    /** 노드 제목에서 과목/레벨 라벨과 "행성" 접미사를 걷어내 스토리에 넣을 학습 주제만 남긴다. */
    static String cleanTopic(String rawTitle, String subjectCode) {
        if (rawTitle == null || rawTitle.isBlank()) {
            return "이 단계";
        }
        String t = rawTitle.trim();
        if (t.endsWith("행성")) {
            t = t.substring(0, t.length() - "행성".length()).trim();
        }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String p : allPrefixes(subjectCode)) {
                if (p == null || p.isBlank()) {
                    continue;
                }
                if (t.length() > p.length()
                        && t.regionMatches(true, 0, p, 0, p.length())
                        && t.charAt(p.length()) == ' ') {
                    t = t.substring(p.length()).trim();
                    changed = true;
                }
            }
        }
        return t.isBlank() ? "이 단계" : t;
    }

    private static String[] allPrefixes(String subjectCode) {
        if (subjectCode == null || subjectCode.isBlank()) {
            return LABEL_PREFIXES;
        }
        String[] merged = new String[LABEL_PREFIXES.length + 1];
        merged[0] = subjectCode;
        System.arraycopy(LABEL_PREFIXES, 0, merged, 1, LABEL_PREFIXES.length);
        return merged;
    }
}
