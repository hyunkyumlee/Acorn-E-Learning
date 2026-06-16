# Layer Contract

Layered SVG는 script와 Rive Editor가 모두 읽기 쉬워야 한다.

## Required Shape

- Canvas: `width="512" height="512" viewBox="0 0 512 512"`
- Background: transparent
- File name: `<slug>-source.svg`
- Groups use `id`; avoid generated names like `Group 123`.

## Preferred IDs

| ID | Purpose |
|---|---|
| `shadow` | ground/drop shadow |
| `mascot` | whole character wrapper |
| `body` | body shell |
| `left-eye` / `right-eye` | eye wrappers for blink |
| `left-eye-white` / `right-eye-white` | white eye ellipses |
| `left-pupil` / `right-pupil` | pupil ellipses |
| `mouth` | mouth/tongue wrapper |
| `cheeks` | cheek blush wrapper |
| `left-arm` / `right-arm` | arm wrappers |
| `left-antenna` / `right-antenna` | antenna wrappers |
| `star-badge` | badge/accessory wrapper |

Missing optional groups are allowed; the render script skips missing IDs.

## Idle Motion Defaults

| Part | Motion |
|---|---|
| `mascot` | subtle vertical float |
| `shadow` | inverse scale to float |
| `left-eye` / `right-eye` | quick blink twice per 3s loop |
| `left-antenna` / `right-antenna` | alternating sway |
| `left-arm` / `right-arm` | small wave |
| `star-badge` | pulse |
| `cheeks` | gentle opacity pulse |

## Rive Naming

Use these names unless the project already has a stronger convention.

| Item | Name |
|---|---|
| Artboard | `Mascot` |
| State machine | `MascotState` |
| Default animation | `idle` |
| Interaction animation | `tap` |
| Success animation | `success` |
| Failure animation | `fail` |
| Loading animation | `loading` |

Expected inputs:

| Input | Type |
|---|---|
| `tap` | trigger |
| `success` | trigger |
| `fail` | trigger |
| `loading` | boolean |
