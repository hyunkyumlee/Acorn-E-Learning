# ULW Notion SRS 작업 notepad

## Brief
- 노션 Knowva 계층에서 `비즈니스 모델`과 `요구사항 명세서`를 합쳐 `SRS` page 작성.
- 원래 BM + RM이 모두 필요하지만, 이번에는 RM을 생략하고 생략 사실과 영향 범위를 SRS에 명시.
- 결과 SRS는 DB 명세, REST API 명세, MVC 명세, 개발 명세 확정의 기준 문서로 사용 가능해야 함.

## Tier
- HEAVY
- 근거:
  - 사용자가 `ulw`, `꼼꼼히`를 명시.
  - 새 기준 문서가 DB/API/MVC/dev spec 확정의 상위 source가 됨.
  - BM, 요구사항, downstream spec page 간 traceability가 필요.

## Source / Target 구분
- Instruction hub: top-level Knowva page
- Primary source: `비즈니스 모델`, `요구사항 명세서`
- Supporting downstream alignment: `DB 명세`, `Rest API 명세`, `MVC 명세`, `화면별 기능 / 데이터 매핑표`, `추적표`
- Editable target: `SRS`

## Success criteria
- C001 source synthesis: Notion fetch/search로 BM, 요구사항 명세서, SRS target 상태를 확인하고 source/target 분리를 evidence로 남김.
- C002 SRS deliverable: `SRS` page가 생성 또는 갱신되고, 문서 정보/개정 이력/source citation/FR/NFR/DR/API/DB/MVC handoff/RM 생략/오픈 이슈를 포함함.
- C003 regression/downstream readiness: 갱신 후 fetch로 핵심 섹션과 downstream handoff 문구가 확인되고, 인접 source page를 삭제/변형하지 않았음을 확인함.
- C004 final review: rigorous reviewer가 evidence와 SRS 구조를 검토해 APPROVE/CLEAR를 반환함.

## Evidence paths
- `.sisyphus/ulw-srs-notion/evidence/c001-source-target.md`
- `.sisyphus/ulw-srs-notion/evidence/c002-srs-created-or-updated.md`
- `.sisyphus/ulw-srs-notion/evidence/c003-downstream-readiness.md`
- `.sisyphus/ulw-srs-notion/evidence/c004-review.md`

