# First 0.2.3 runtime capture and 0.2.4 candidate

Source: `factoryprobe-1792779455561.txt`, 30.04 seconds, 1,001 rendered frames.
The log confirms all nine elapsed-time scopes were loaded and no diagnostic errors
were reported. Minecraft used RTX 5070 Laptop GPU / NVIDIA 617.42, Java 8u51,
without Angelica or OptiFine. The capture began roughly six seconds after joining;
loading, cold caches and JIT warm-up may influence the result.

| Measured scope | Total time | Per rendered frame |
| --- | ---: | ---: |
| Camera/world/GUI rendering | 22.501 s | 22.479 ms |
| Vanilla section rebuild method | 7.332 s | 7.324 ms |
| Entities and tile rendering | 5.948 s | 5.942 ms |
| Terrain draw | 2.990 s | 2.987 ms |
| Client tick | 4.840 s | 4.835 ms |
| Display update | 2.146 s | 2.144 ms |
| GL flush | 0.072 s | 0.072 ms |
| FPS limiter | 0.002 s | 0.002 ms |

Rendering scopes are nested; do not sum them. They measure elapsed CPU/driver time,
not actual GPU execution. The current scopes establish expensive rendering and
rebuild work in this run, without distinguishing every contributor to that work.

Average FPS was **33.3**, with p50 26.803 ms, p95 54.055 ms, p99 81.938 ms and worst
240.366 ms. The local server completed 600 ticks, about 20/s, averaging 22.527 ms per
tick and reaching 305.231 ms worst. It had occasional stalls, but sustained low
client FPS in this capture is not explained by a persistent server tick-rate collapse.

Examples of rebuild-related stalls:

- A 104.539 ms frame contained 73.106 ms of section rebuild work.
- A 98.581 ms frame contained 75.644 ms of rebuild work.
- A 97.854 ms frame contained 55.758 ms of rebuild work.
- The largest, 240.366 ms frame overlapped 144 ms of GC collection-time increase;
  it is not a pure rebuild event.

The sampled invalidation paths included 699 multi-block-change samples, 422 samples
inside Ender itemduct full-description handling, 208 outer CoFH packet samples,
plus chunk reception, AE2 and smaller sources. Sampling is one in 16 requests,
not a census of initiating blocks or completed section rebuilds. Many causes can
coalesce into one rebuilt section. The snapshot does not yet establish how many
Ender descriptions are identical, because 0.2.3 filters/counts only plain ducts.

The 0.2.4 candidate covers exact `TileItemDuctEnder` instances using the existing
full-description duplicate filter. Local bytecode inspection confirms its
description adds a final power boolean, while separate power changes arrive as
info packets. The new common info-packet entry hook invalidates cached descriptions
for supported ducts before processing those updates. Changed payloads, first
packets, replacements, changed world/position, periodic refreshes and unsupported
streams still redraw. All original packet data is consumed and handled normally.

The candidate does not throttle world updates or server simulation, cap mesh result
batches, hide blocks, or change machine contents. It may reduce repeated rebuilds
when identical Ender descriptions occur without intervening info updates. The
frequency and FPS benefit need a live comparison; it is not a confirmed complete fix.

Validation: plain/Ender packet behavior and stream preservation; separate info/power
invalidation; changed/replacement/world/position/periodic redraws; disabled/server
behavior and unsupported subclasses; ASM verification on real CoFH/TD classes;
timing hooks in MCP/SRG namespaces; existing frame/capture regression checks. The
four timing/loading companion classes are byte-identical to 0.2.3. Test fixtures
are excluded from the mod JAR.

Built JAR SHA-256:
`1F8A69711F337D37CE8F409A2FAC0AF1A74F4850932A9BA16D98205E798C4F0D`

After installing, join and allow at least a minute for loading to settle before
recording F9 over the same route. Compare rebuild elapsed time, frame intervals,
Ender duplicate counts and visible correctness, not just the F3 update counter.
