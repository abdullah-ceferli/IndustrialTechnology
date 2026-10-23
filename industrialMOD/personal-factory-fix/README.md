# Personal HBM factory patch investigation

Version 0.2.1 includes an experimental redundant-redraw filter for plain Thermal
Dynamics 1.2.1 itemducts, plus the diagnostic companion. A stationary 30-second
comparison in the affected world measured 38.8 FPS with the filter off and 62.2 FPS
with it on. This is one instrumented comparison, not a general FPS guarantee or a
complete fix for the reported regression. Version 0.2.0 failed open in production because its field lookup did
not find the inherited, protected world reference; 0.2.1 corrects that lookup.
It supports Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8 and does not require Angelica.
It always processes packet data, but suppresses duplicate client redraw requests
when the entire received duct description is identical to the previous description
for the same tile instance. Changed descriptions redraw immediately. Identical data
still forces a redraw at least once per second. Server logic and other tile types
are unchanged. It never removes blocks or changes machine contents. F10 toggles the
redraw filter for comparison; F9 records a diagnostic capture. Install only one
Factory Diagnostics/Factory Fix JAR at a time.

The first loaded world also starts one capture automatically after a 15-second warmup.
In a loaded world, face the affected factory and tap **F9** for another capture. Leave the view still for
30 seconds. A report appears in the game's `logs/factoryprobe-<timestamp>.txt`.
F9 again ends capture early. Capture includes loading/menu/pause time if used there;
only record in the world. The diagnostic sampling itself adds overhead, so FPS is
not an uninstrumented benchmark. Renderer timings include CPU work and driver waits.
Region centers can be adjacent to the actual source; call stacks are sampled, not
proof that the block at a sampled coordinate initiated the update.

Measured on 2026-10-22 with NVIDIA 617.42, Java 8u51 and no Angelica or OptiFine:

| 30-second capture, same stationary view | Filter off | Filter on |
| --- | ---: | ---: |
| Average FPS | 38.8 | 62.2 |
| Render invalidation requests | 26,690 | 7,018 |
| Redundant redraw calls skipped | 0 | 18,713 |

These are invalidation requests, not counts of actual rebuilt chunks. Factory
activity continued during the sequential captures. The probe samples requests, so
part of the FPS difference can come from reduced diagnostic work when fewer
requests are made. Normal play without a capture has not been benchmarked here.
The observed 120–150 FPS from earlier sessions has not been restored or explained.
Raw reports and installation details are in `verification/2026-10-22/`.
