# Service Integration

Use fallback animation first, then swap to Rive when `.riv` is exported.

## HTML Fallback

```html
<video class="mascot-video" autoplay loop muted playsinline poster="/assets/characters/nova/exports/nova-poster.png">
  <source src="/assets/characters/nova/exports/nova-idle.webm" type="video/webm">
  <img src="/assets/characters/nova/exports/nova-poster.png" alt="Nova mascot">
</video>
```

Autoplay safeguard:

```html
<script>
  const mascotVideo = document.querySelector(".mascot-video");
  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

  function playMascotVideo() {
    if (!mascotVideo || reduceMotion.matches) return;
    const playRequest = mascotVideo.play();
    if (playRequest) {
      playRequest.catch(() => mascotVideo.setAttribute("data-playback", "poster"));
    }
  }

  if (mascotVideo) {
    if (mascotVideo.readyState >= 2) playMascotVideo();
    else mascotVideo.addEventListener("loadeddata", playMascotVideo, { once: true });
  }
</script>
```

## React Rive Runtime

```tsx
import { useRive, useStateMachineInput } from "@rive-app/react-canvas";

type MascotEvent = "idle" | "success" | "fail";

export function Mascot({ event }: { readonly event: MascotEvent }) {
  const { rive, RiveComponent } = useRive({
    src: "/assets/characters/nova/nova.riv",
    artboard: "Mascot",
    stateMachines: "MascotState",
    autoplay: true,
  });

  const success = useStateMachineInput(rive, "MascotState", "success");
  const fail = useStateMachineInput(rive, "MascotState", "fail");

  if (event === "success") success?.fire();
  if (event === "fail") fail?.fire();

  return <RiveComponent />;
}
```

Use the project framework's lifecycle hooks for repeated event changes. Keep a poster or video fallback for runtime/network failure.
