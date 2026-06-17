# Alien UFO Asset Contract

## Files

| File | Purpose |
|---|---|
| `alien-ufo-source.svg` | layered source, Rive Editor import target |
| `alien-ufo-concept-sheet.png` | 3-option still contact sheet |
| `alien-ufo-selected-still.png` | selected still poster |
| `exports/alien-ufo-poster.png` | idle poster frame |
| `exports/alien-ufo-idle.webm` | idle bob fallback |
| `exports/alien-ufo-idle.apng` | idle APNG fallback |
| `exports/alien-ufo-idle.gif` | idle GIF fallback |
| `exports/alien-ufo-idle-sprite.png` | idle sprite strip |
| `exports/alien-ufo-booster.webm` | booster movement fallback |
| `exports/alien-ufo-booster.apng` | booster APNG fallback |
| `exports/alien-ufo-booster.gif` | booster GIF fallback |
| `exports/alien-ufo-booster-sprite.png` | booster sprite strip |
| `exports/alien-ufo-booster-poster.png` | booster poster frame |
| `alien-ufo-qa-capture.png` | browser playback QA capture |
| `variations/alien-ufo-expression-sheet.png` | expression variation preview |
| `variations/alien-ufo-direction-sheet.png` | direction variation preview |
| `variations/expressions/*.svg` | individual expression SVG assets |
| `variations/directions/*.svg` | individual direction SVG assets |
| `variations/alien-ufo-variation-guide.md` | variation usage guide |

## Rive Handoff

`.riv` binary는 직접 만들지 않았어. Rive Editor에서 `alien-ufo-source.svg`를 import한 뒤 아래 contract로 export하면 돼.

| Item | Name |
|---|---|
| Artboard | `Mascot` |
| State machine | `MascotState` |
| Default animation | `idle` |
| Booster animation | `booster` |

## State Machine Inputs

| Input | Type | Meaning |
|---|---|---|
| `booster` | boolean | `true`면 booster movement, `false`면 idle |
| `tap` | trigger | optional tap reaction |

## Layer IDs

| ID | Role |
|---|---|
| `mascot` | whole character wrapper |
| `shadow` | ground shadow |
| `body` | alien head/body wrapper |
| `left-eye`, `right-eye` | blink wrappers |
| `left-eye-white`, `right-eye-white` | eye geometry anchors |
| `left-pupil`, `right-pupil` | dark pupil layers |
| `mouth` | smile |
| `cheeks` | cheek tint |
| `left-arm`, `right-arm` | hands over cockpit rim |
| `left-antenna` | center antenna, mapped to pipeline antenna motion |
| `star-badge` | orange saucer lights, reused as pulse group |
| `flames` | booster flame wrapper |
| `boost-trails` | vertical thrust trails |

## Runtime Recommendation

Rive export 전에는 `exports/alien-ufo-idle.webm`을 기본 mascot idle로 쓰고, 이동/전환 상태에서는 `exports/alien-ufo-booster.webm`을 쓰면 돼.
