# Knowva Rive Assets

이 디렉토리는 Knowva mascot animation asset 자리야.

## Files

| File | Purpose |
|---|---|
| `knowva-mascot-source.svg` | Rive Editor에 import할 layered source |
| `knowva-mascot-placeholder.riv` | Rive runtime smoke test용 placeholder |
| `exports/knowva-mascot-idle.webm` | Rive export 전 서비스 preview/fallback |
| `exports/knowva-mascot-poster.png` | 정적 fallback poster |
| `knowva-mascot.riv` | production export 대상 파일명 |

## Production Contract

Rive Editor에서 export할 때 아래 이름을 유지해.

| Item | Name |
|---|---|
| Artboard | `Mascot` |
| State machine | `MascotState` |
| Idle animation | `idle` |
| Tap animation | `tap` |
| Success animation | `success` |
| Fail animation | `fail` |
| Loading animation | `loading` |

서비스에서는 `knowva-mascot.riv`만 이 경로에 배치하면 돼.
