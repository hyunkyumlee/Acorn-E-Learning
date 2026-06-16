# Knowva Rive Mascot Integration

목표: 캐릭터 모션을 HTML/CSS 장식이 아니라 Rive `.riv` asset으로 분리해서 서비스 이벤트에 연결한다.

## 현재 산출물

| File | Role |
|---|---|
| `rive-mascot-bridge.html` | Rive runtime smoke sample |
| `assets/rive/knowva-mascot-source.svg` | Rive Editor import용 layered vector source |
| `assets/rive/knowva-mascot-placeholder.riv` | runtime 검증용 placeholder |
| `assets/rive/exports/knowva-mascot-idle.webm` | Rive export 전 서비스 preview/fallback |
| `assets/rive/exports/knowva-mascot-poster.png` | 정적 fallback poster |
| `assets/rive/knowva-mascot.riv` | 실제 production export 대상 |

## Rive Editor 작업

1. `assets/rive/knowva-mascot-source.svg`를 Rive Editor에 import한다.
2. Artboard 이름을 `Mascot`으로 맞춘다.
3. State machine 이름을 `MascotState`로 만든다.
4. 아래 animation clip을 만든다.

| Clip | Motion |
|---|---|
| `idle` | body breathing, eye blink, antenna sway |
| `tap` | right arm wave, body squash |
| `success` | hop, star pulse, cheeks pop |
| `fail` | body dip, eyes soften, antenna droop |
| `loading` | gentle float loop |

5. export 파일명을 `knowva-mascot.riv`로 저장한다.
6. `docs/ui-samples/assets/rive/knowva-mascot.riv`에 배치한다.
7. 아래 URL로 열어 placeholder 대신 production mascot이 뜨는지 확인한다.

```text
rive-mascot-bridge.html?src=./assets/rive/knowva-mascot.riv&stateMachine=MascotState
```

## Service Contract

| Field | Value |
|---|---|
| Public asset path | `/assets/rive/knowva-mascot.riv` |
| Artboard | `Mascot` |
| State machine | `MascotState` |
| Default state | `idle` |
| Fallback | `exports/knowva-mascot-idle.webm` or static poster |

## React Hook Shape

```tsx
import { useRive } from "@rive-app/react-canvas";

export function KnowvaMascot() {
  const { RiveComponent } = useRive({
    src: "/assets/rive/knowva-mascot.riv",
    artboard: "Mascot",
    stateMachines: "MascotState",
    autoplay: true,
  });

  return <RiveComponent />;
}
```

실제 React 서비스가 생기면 이 component를 학습 화면의 `StatusPanel` 또는 mission CTA 주변에 배치한다.

## HTML Fallback Shape

```html
<video autoplay loop muted playsinline poster="/assets/rive/exports/knowva-mascot-poster.png">
  <source src="/assets/rive/exports/knowva-mascot-idle.webm" type="video/webm">
  <img src="/assets/rive/exports/knowva-mascot-poster.png" alt="Nova buddy">
</video>
```
