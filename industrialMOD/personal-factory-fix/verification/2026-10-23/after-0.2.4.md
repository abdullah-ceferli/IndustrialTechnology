# 0.2.4 recording and vanilla-only next step

The user chose to **keep Angelica removed**. It must not be restored as an assumed
optimization. Current installed patch is 0.2.4.

| Measurement | 0.2.3 recording | 0.2.4 recording |
| --- | ---: | ---: |
| Duration | 30.04 s | 30.03 s |
| Average FPS | 33.3 | 44.1 |
| p95 frame interval | 54.055 ms | 37.808 ms |
| p99 frame interval | 81.938 ms | 51.146 ms |
| Worst interval | 240.366 ms | 140.790 ms |
| Camera/world/GUI time per frame | 22.479 ms | 18.535 ms |
| Section rebuild time per frame | 7.324 ms | 5.813 ms |
| Section rebuild time, whole capture | 7.332 s | 7.690 s |
| Section rebuild method calls | 1,272 | 2,383 |
| Redraw requests | 25,850 | 11,451 |
| Ender description packets eligible for filter | not separately counted | 0 |

These are not controlled before/after measurements. The first run was shortly
after joining; the second had a different route/view and about five seconds paused.
Its paused bins had no server ticks and roughly 60 FPS. The 498 server ticks over
the entire second capture are not evidence of persistent 16.6 TPS lag; the server
was paused for part of it. The reported average improvement cannot be attributed
to the new Ender filter, which processed no packets in this capture. There were
no diagnostic errors or unsupported-hook warnings in the game log.

Rendering remains the largest measured frame cost. Of 716 sampled invalidation
stacks, 611 came through multi-block changes and 35 through single-block changes.
Sampled positions often contained radon/asbestos, air, or water; they are request
region centers and do not prove which block initiated each request.

An experimental 0.2.5 candidate altered only the client redraw flag for invisible
air/radon/asbestos transitions in an otherwise invisible 5x5x5 halo, with unchanged
lighting properties, loaded neighboring chunks and no center tile entity. Data
and metadata still passed through the original setBlock call. It passed guard,
fallback, transformed-execution and real MCP/SRG bytecode checks. However, a
read-only saved-geometry estimate found only **1 of 176** nearby radon/asbestos
blocks had such a clear halo. This is a static estimate, not a live packet census,
but offers too little evidence to justify installing another candidate. The
experiment is archived and its integration was removed from the active source.
No gas blocks or other world data were edited.

Read-only power checks: AC connected, Turbo Windows scheme, AC maximum processor
state 100%. The idle GPU reading cannot rule out thermal/power constraints under
game load. No power or driver settings were changed.

A reversible standard vanilla preset was prepared:

- Render distance 16 to 8.
- Fancy graphics to Fast.
- Smooth Lighting maximum to Off.
- Clouds Off.
- Particles All to Decreased.

This trades view distance and visual effects for rendering/meshing work. It is not
yet a measured FPS improvement. maxFps 120 and VSync false are preserved. Options
are backed up before any application. The installer currently waits for Minecraft
to close; the attempt during this turn found PID 25676 and changed nothing.

After applying, allow one minute of **unpaused** in-world activity, then record the
same route with F9 for 30 seconds without opening pause menus. Judge normal-play
smoothness too; profiling adds overhead. No additional experimental mod is needed
for this test.
