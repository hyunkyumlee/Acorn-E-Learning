# Art Quality Gate

이 gate를 통과하지 못하면 layered SVG나 animation으로 진행하지 않는다.

## Still Mascot Acceptance

| Check | Pass 기준 |
|---|---|
| Silhouette | 96px thumbnail에서도 어떤 캐릭터인지 읽힘 |
| Proportion | 머리/몸/팔다리 비율이 의도한 귀여움에 맞고 찌그러져 보이지 않음 |
| Eye spacing | 미간이 너무 좁거나 넓지 않음. 눈 사이 간격은 한쪽 눈 폭의 0.45-0.9배를 기본값으로 봄 |
| Eye alignment | 좌우 눈 높이와 시선 방향이 일관됨 |
| Mouth placement | 입이 눈과 볼 사이에서 자연스럽고 표정 의도가 명확함 |
| Accessory scale | 별, 가방, 안테나, 하트 같은 accessory가 얼굴을 먹지 않음 |
| Material | 원본이 glossy/3D/toy 느낌이면 highlight, soft shadow, rim light가 있어야 함 |
| Crop margin | 512x512 안에서 antenna/accessory가 잘리지 않고 8-12% 여백이 있음 |
| Mobile readability | 80-120px 크기에서도 표정과 핵심 accessory가 보임 |

## Common Failure Fixes

| Symptom | Fix |
|---|---|
| 쫌생이처럼 보임 | 눈을 바깥쪽으로 벌리고 동공을 살짝 중앙 쪽으로 둔다 |
| 멍청하게 벌어져 보임 | 눈은 유지하고 동공/하이라이트를 중앙 쪽으로 조정한다 |
| 구형 body가 뭉개짐 | body highlight와 shadow를 늘리고 외곽 rim contrast를 올린다 |
| 싼티나는 vector | flat fill만 쓰지 말고 2-3단계 gradient, specular highlight, contact shadow를 넣는다 |
| accessory가 방해됨 | accessory를 10-20% 줄이고 body 하단/측면으로 이동한다 |
| animation에서 얼굴이 깨짐 | blink는 eye wrapper scale만 움직이고 pupil 위치를 따로 비틀지 않는다 |

## Contact Sheet Review

후보는 최소 3개, 가능하면 6개를 만든다.

각 후보에 짧은 label을 붙인다.

```text
A: closest to source
B: rounder baby proportion
C: cleaner product mascot
D: more 3D toy-like
```

선택 전에 한 줄 판단을 남긴다.

```text
Pick B: source likeness is slightly lower, but thumbnail readability and eye spacing are strongest.
```
