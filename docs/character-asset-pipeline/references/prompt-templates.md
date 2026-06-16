# Prompt Templates

이미지 생성 도구가 가능하면, 원본 이미지를 참고해 먼저 high-quality still mascot 후보를 만든다. 후보가 통과해야 SVG/Rive 작업으로 간다.

## Character Recreation Prompt

```text
Create a polished production mascot based on the provided character reference.
Keep the core identity: [body shape], [main colors], [eyes], [mouth], [accessory].
Style: soft glossy 3D toy mascot, rounded forms, clean silhouette, studio product lighting, transparent background.
Improve proportions for service UI: readable at 96px, balanced eye spacing, cute but not cramped, clear expression.
Output one centered full-body character, no text, no background, no extra characters.
```

## Contact Sheet Prompt

```text
Create 6 production mascot variations based on the provided character reference.
Arrange as a clean 2x3 contact sheet on a plain light background.
All variations must keep the same identity and color family.
Vary only proportions, eye spacing, accessory scale, and expression.
Avoid distorted limbs, cramped eyes, asymmetry, extra characters, text, or cropped antennae.
```

## Quality Repair Prompt

Use when the current output is close but ugly.

```text
Refine this mascot without changing its identity.
Fix these issues: [too narrow eye spacing / awkward head-body ratio / weak 3D material / accessory too large].
Keep: [colors], [accessory], [expression direction].
Improve: balanced facial proportions, glossy toy-like shading, clean silhouette, mobile thumbnail readability.
Transparent background, centered full-body, no text.
```

## Layering Prompt

Use after a still mascot is selected.

```text
Convert the selected mascot into a clean layered vector-style source for animation.
Separate visible parts conceptually: body, left eye, right eye, pupils, mouth, cheeks, arms, antennae, accessory, shadow.
Keep the final appearance close to the selected still mascot.
Use simple shapes, gradients, highlights, and soft shadows.
Transparent background, 512x512 composition.
```

## Negative Constraints

Always include these when generation quality drifts:

```text
no text, no logo, no extra characters, no cropped body, no deformed eyes, no mismatched pupils,
no flat cheap clipart, no harsh outline, no busy background, no realistic animal texture
```
