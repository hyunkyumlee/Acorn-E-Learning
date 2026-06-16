---
name: character-asset-pipeline
description: Create high-quality mascot or character animation assets from a user-provided character image, screenshot, sketch, or style reference. Use when the user asks to turn a character photo into service-ready assets, improve mascot quality, create character concept options, make Rive-ready layered SVG, idle animation, WebM/APNG/GIF fallback, sprite sheet, poster image, or frontend integration snippets.
---

# character-asset-pipeline

## Goal

입력 캐릭터 이미지를 서비스에 넣을 수 있는 animation asset package로 만든다. 먼저 좋은 정지 캐릭터를 만들고, 그다음 layered SVG, animated fallback files, Rive handoff contract, integration snippet, QA capture를 만든다.

## Hard Boundary

- `.riv` binary를 직접 위조하지 않는다.
- 현재 Rive authoring CLI나 Rive Editor 접근이 없으면 `.riv`는 handoff target으로만 둔다.
- Rive Editor export가 필요하면 `artboard`, `state machine`, input names를 문서화하고 fallback asset을 먼저 서비스에 연결한다.
- 캐릭터 원본의 상업권/사용권이 불명확하면 최종 응답에 `권리 확인 필요`를 표시한다.
- 수작업 SVG primitive부터 시작하지 않는다. 먼저 high-quality still mascot 후보를 만든 뒤 선택된 안만 레이어화한다.
- 사용자가 “스킬만 고쳐줘”처럼 범위를 좁히면 project assets는 수정하지 않는다.

## Workflow

1. 입력 이미지를 확인한다.
   - 캐릭터 형태, 눈/입/팔/안테나/accessory, 색상, 질감을 요약한다.
   - 정확한 복제가 위험하거나 권리가 불명확하면 “inspired/recreated asset”으로 만든다.

2. 출력 위치를 정한다.
   - 프로젝트가 있으면 기존 asset convention을 따른다.
   - convention이 없으면 `<target>/assets/characters/<slug>/`를 쓴다.
   - 임시 실험은 `/tmp/<slug>-asset-pipeline/`에 만들고, 채택된 결과만 프로젝트로 옮긴다.

3. quality direction을 만든다.
   - `references/art-quality-gate.md`를 읽는다.
   - 원본 비율, 핵심 표정, silhouette, material, eye spacing, accessory scale을 정리한다.
   - 사용자가 직접 한 캐릭터를 지정하지 않았고 이미지에 여러 캐릭터가 있으면 한 캐릭터 crop을 먼저 확정한다.

4. concept 후보를 만든다.
   - `references/prompt-templates.md`를 읽는다.
   - 가능하면 3-6개 high-quality still mascot 후보를 contact sheet로 만든다.
   - 후보 선택 전에는 layered SVG, animation export, service insertion을 시작하지 않는다.
   - 후보가 구리면 prompt를 고쳐 한 번 더 생성한다. bad still을 animation으로 밀어붙이지 않는다.

5. 선택안에 quality gate를 적용한다.
   - head/body ratio, eye spacing, mouth placement, limb/accessory scale, crop margin, mobile thumbnail readability를 확인한다.
   - 실패하면 still mascot 단계로 돌아간다.

6. layered SVG source를 만든다.
   - 먼저 `references/layer-contract.md`를 읽는다.
   - 512x512 viewBox를 기본으로 쓴다.
   - 주요 part는 stable `id`를 붙인다: `mascot`, `body`, `left-eye`, `right-eye`, `mouth`, `left-arm`, `right-arm`, `left-antenna`, `right-antenna`, `cheeks`, `star-badge`, `shadow`.
   - source 파일명은 `<slug>-source.svg`로 둔다.

7. fallback animation을 렌더링한다.
   - layered SVG가 준비되면 script를 실행한다.

```bash
node /Users/hyunkyumlee/.codex/skills/character-asset-pipeline/scripts/render-layered-svg.mjs \
  --source path/to/<slug>-source.svg \
  --out path/to/exports \
  --slug <slug>
```

8. 서비스 연결 파일을 만든다.
   - Rive export 전에는 `exports/<slug>-idle.webm` + `exports/<slug>-poster.png`를 사용한다.
   - Rive export 후에는 `<slug>.riv`를 runtime component에 연결한다.
   - HTML/React snippet이 필요하면 `references/service-integration.md`를 읽는다.

9. QA한다.
   - 브라우저에서 실제 target screen을 연다.
   - video ready state, autoplay/muted/loop, console errors, desktop/mobile overflow를 확인한다.
   - poster/sprite를 직접 열어 얼굴 비율, 미간, blink, cropping을 본다.

## Output Package

기본 산출물:

```text
<slug>-reference-notes.md
<slug>-concept-sheet.png
<slug>-selected-still.png
<slug>-source.svg
exports/<slug>-poster.png
exports/<slug>-idle.webm
exports/<slug>-idle.apng
exports/<slug>-idle.gif
exports/<slug>-idle-sprite.png
<slug>-asset-contract.md
```

`.riv` export가 완료되면 추가:

```text
<slug>.riv
```

## Quality Bar

- 캐릭터가 원본 이미지의 핵심 인상은 유지하되 서비스 UI에 맞게 단순화되어야 한다.
- still mascot이 별로면 실패다. animation은 나쁜 캐릭터를 구제하지 못한다.
- 눈 간격, 표정, head/body ratio, accessory는 poster와 sprite에서 직접 확인한다.
- 후보 없이 1안으로 바로 확정하지 않는다. 사용자가 빠른 초안을 명시한 경우에도 최소 3안 contact sheet를 만든다.
- mobile에서 text/card/video가 겹치면 완료가 아니다.
- animated fallback은 512x512, transparent background, 2-4초 loop를 기본으로 한다.
