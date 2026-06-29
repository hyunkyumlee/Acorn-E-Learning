# Knowva Design System

## 1. Atmosphere & Identity

Knowva feels like a focused learning control room: calm, structured, and slightly orbital without becoming decorative. The signature is an ion-line header: a sticky, glass-like command bar with compact navigation, a clear active state, and a profile/action cluster that stays usable across learning, analysis, community, mypage, settings, and admin flows.

## 2. Color

### Palette

| Role | Token | Light | Dark | Usage |
|---|---|---|---|---|
| Surface/base | `--bg` | `#f6fbfd` | `#0d1720` | Page background |
| Surface/primary | `--surface` | `#ffffff` | `#152230` | Cards, panels, header |
| Surface/secondary | `--surface-soft` | `#eef8fb` | `#1d2d3b` | Soft panels and active nav |
| Surface/header | `--header-bg` | `#f7fdff` | `#101d28` | Top navigation background |
| Text/primary | `--text` | `#132331` | `#f5f9fc` | Headlines and body |
| Text/secondary | `--muted` | `#607181` | `#aab8c5` | Captions and secondary copy |
| Border/default | `--line` | `#d9e7ee` | `#2e4354` | Dividers and card borders |
| Accent/primary | `--primary` | `#22b8c8` | `#64d9e6` | Active navigation, focus, primary controls |
| Accent/strong | `--accent` | `#0a4f5b` | `#8beaf1` | Brand mark and emphasized text |
| Status/success | `--success` | `#15803d` | `#4ade80` | Success states |
| Status/warning | `--warning` | `#b7791f` | `#facc15` | Warning states |
| Status/error | `--danger` | `#b42318` | `#fb7185` | Error and destructive states |

### Rules

- Use the teal ion accent only for navigation state, focus, primary actions, and progress emphasis.
- Body surfaces stay quiet and mostly neutral; avoid one-note blue or purple pages.
- New colors must be added here before use.

## 3. Typography

### Scale

| Level | Size | Weight | Line Height | Tracking | Usage |
|---|---:|---:|---:|---:|---|
| Display | 40px | 900 | 1.1 | 0 | Large route titles |
| H1 | 32px | 850 | 1.2 | 0 | Page headings |
| H2 | 22px | 800 | 1.3 | 0 | Panel headings |
| H3 | 18px | 750 | 1.35 | 0 | Card titles |
| Body | 16px | 400 | 1.6 | 0 | Default text |
| Body/sm | 14px | 500 | 1.5 | 0 | Secondary text |
| Caption | 12px | 700 | 1.4 | 0 | Labels and metadata |
| Brand | 26px | 900 | 1 | 0 | Header brand wordmark |

### Font Stack

- Primary: system UI stack, `-apple-system`, BlinkMacSystemFont, `"Segoe UI"`, sans-serif.
- Mono: `"SFMono-Regular"`, Consolas, `"Liberation Mono"`, monospace.

### Rules

- Body text does not go below 14px.
- Letter spacing remains 0 unless the text is a small uppercase label.

## 4. Spacing & Layout

### Base Unit

All spacing derives from 4px.

| Token | Value | Usage |
|---|---:|---|
| `--space-1` | 4px | Icon gaps |
| `--space-2` | 8px | Compact groups |
| `--space-3` | 12px | Form padding |
| `--space-4` | 16px | Default gaps |
| `--space-5` | 20px | Card inner rhythm |
| `--space-6` | 24px | Panel padding |
| `--space-8` | 32px | Section gaps |
| `--space-10` | 40px | Major page spacing |
| `--space-12` | 48px | Header-to-content separation |

### Grid

- Max content width: `1180px`.
- Header grid: brand, centered navigation, action cluster.
- Breakpoints: compact header behavior starts at 880px and 640px.

### Rules

- Fixed-format controls use stable dimensions, especially icon buttons and pills.
- Avoid nested cards; panels sit directly on the page shell.

## 5. Components

### App Topbar

- **Structure**: sticky `header.topbar` with brand, `nav.topbar-nav`, and `.topbar-actions`.
- **Variants**: authenticated app topbar, guest header.
- **Spacing**: `--space-2`, `--space-3`, `--space-4`.
- **States**: hover, focus-visible, active nav, active theme toggle.
- **Accessibility**: semantic `header`/`nav`, `aria-label`, `aria-current`, visible focus ring.
- **Motion**: transform and color transitions only.

### Page Shell

- **Structure**: `main.page-shell` with `.page-heading` and `.panel`.
- **Variants**: route skeleton pages, form pages, error pages.
- **Spacing**: `--space-5`, `--space-6`, `--space-8`.
- **Accessibility**: one primary `h1`, readable text contrast, no layout overlap.

### Controls

- **Structure**: `.button`, `.header-icon`, `.profile-pill`.
- **Variants**: default, primary, icon-only, pill.
- **States**: hover, focus-visible, active, disabled.
- **Motion**: 160ms transform/background/color transitions.

## 6. Motion & Interaction

| Type | Duration | Easing | Usage |
|---|---:|---|---|
| Micro | 160ms | ease-out | Header hover, button hover |
| Standard | 220ms | ease-in-out | Theme color transition |

### Rules

- Animate only `transform`, `opacity`, `filter`, `background`, `color`, and `border-color`.
- Respect `prefers-reduced-motion`.
- Every interactive control has a visible focus state.

## 7. Depth & Surface

### Strategy

Use mixed depth: light tonal surfaces, 1px borders for structure, and restrained shadows only for the sticky header and hover affordances.

| Level | Value | Usage |
|---|---|---|
| Border/default | `1px solid var(--line)` | Panels, nav container, inputs |
| Shadow/subtle | `var(--shadow-soft)` | Header, hover states |
| Shadow/elevated | `var(--shadow-elevated)` | Floating panels or future dialogs |
