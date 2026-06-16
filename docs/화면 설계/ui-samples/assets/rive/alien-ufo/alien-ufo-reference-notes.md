# Alien UFO Reference Notes

## Source

- Reference: `/Users/hyunkyumlee/Downloads/image (1).png`
- Input size: 566x524 PNG
- Usage status: 권리 확인 필요. 원본의 상업권이 명확하지 않아서 production에는 확인 후 써야 해.

## Character Read

- 핵심 silhouette: 투명한 glass dome 안의 초록 alien, 납작한 UFO saucer, 아래 booster flame.
- 얼굴: 작은 dark oval eyes, tiny white highlights, 짧은 U-shaped smile.
- 비율: 큰 둥근 머리, 작은 몸통과 손, 한 개 antenna.
- Material: glossy toy-like alien skin, translucent blue dome, cream/teal saucer, orange/yellow lights.
- 원본 유지 기준: alien 색상, dome/UFO 구조, orange lights, bottom booster flame은 유지. 배경의 별/행성은 service asset에서는 제거.

## Concept Sheet Decision

| Option | Direction | Decision |
|---|---|---|
| A | Closest to source, compact alien in UFO | 선택 |
| B | Rounder baby alien, larger head and dome | 보류 |
| C | Stronger booster emphasis, larger flames | booster animation 참고 |

Pick A: 원본 인상이 가장 강하고, 96px thumbnail에서도 UFO와 얼굴이 같이 읽혀.

## Quality Gate

| Check | Result |
|---|---|
| Silhouette | dome + saucer + flame 구조가 96px에서도 읽힘 |
| Proportion | alien head/body, saucer scale, dome margin을 원본에 가깝게 유지 |
| Eye spacing | 원본처럼 넓은 간격의 small dark eyes 유지 |
| Mouth placement | 눈 아래 중앙의 짧은 smile 유지 |
| Accessory scale | antenna와 dome이 crop 밖으로 나가지 않음 |
| Material | gradient, highlight, soft shadow 포함 |
| Crop margin | 512x512 기준 위 antenna와 아래 flame에 여백 확보 |

