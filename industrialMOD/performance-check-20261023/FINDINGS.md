# Minecraft FPS investigation — 23 October 2026

The original 120–150 FPS regression is **not fully explained or resolved**. A repeatable partial improvement was found with the earlier rendering stack. The original saved world was never launched by these tests and all 795 source world file hashes still match.

The installed Industrial Technology 1.6.3 JAR exactly matches the pre-space baseline (SHA256 `5720b842a84e6ca365efe5d3951425434e13303f1846e7d4e29ce3e5b4f59e98`). No space implementation is active in the installed game. Older launch logs loaded OptiFine E7; the current launch used vanilla rendering and newer CodeChicken dependencies. Existing F9 reports also show substantial section rebuild/render work. Those observations alone do not establish the entire original cause.

## Performed comparisons

Independent copies of the same save, same copied configuration and mods, same saved base position/view, 1280x720 window, render distance as listed, 120 FPS limit, normal survival simulation, and no manual F9 capture. Each recorded sample is about 20 seconds after 30 seconds initial settling or 20 seconds after a setting change. All Java launches use the same 8u51 runtime except the explicitly labeled 8u504 comparison.

| Setup | Render distance | Average FPS | Frame p95, ms |
|---|---:|---:|---:|
| Current renderer | 16 | 41.50 | 36.221 |
| Current renderer | 8 | 45.19 | 39.235 |
| OptiFine, newer CodeChicken | 16 | 52.24 | 29.669 |
| OptiFine, original CodeChicken | 16 | 62.05 | 25.758 |
| Same old stack, Java 8u504 | 16 | 60.22 | 27.221 |
| Old stack, Fast Render comparison (Fast Render=false) | 16 | 63.66 | 25.827 |
| Old stack, Fast Render comparison (Fast Render=true) | 16 | 64.03 | 25.695 |

Fast Render showed no useful benefit and remains off. Lowering distance alone also did little in this view; the installed distance remains 16. The newer Java runtime did not improve this sample, so Java launch settings were retained. Both Fast Render screenshots were inspected and reported no OpenGL error.

Limitations: these are short stationary comparisons, not a controlled long-session benchmark. Factory simulation, JVM warm-up, scheduling and driver timing vary. Fullscreen resolution, movement, other base views, and long play sessions can give different FPS. Test login uses a diagnostic username; the single-player inventory/player position loads from the copied save, but separately keyed per-player mod data can differ. The initial client event briefly shows the default pre-sync position; every measured result above verifies the actual saved base position. The current-renderer run was stopped after its two completed samples, so the planned final 16-chunk control was not recorded. No results are claimed for that missing control.

## Applied reversible change

Restored the original CodeChickenCore 1.0.7.48 / CodeChickenLib 1.1.3.141 pair and the existing OptiFine 1.7.10 E7 JAR, which were tested together in the copies. The newer CodeChickenCore 1.4.22 / CodeChickenLib 1.3.0 files were moved into `applied-renderer-backup/old-mods/`. The installed JAR set is verified to match the tested old-stack copy, excluding the test-only helper. PersonalFactoryFix 0.2.4, Industrial Technology, all other mods, graphics options, Java settings and game save data were left as they were.

Rollback while Minecraft is closed:

```powershell
powershell -ExecutionPolicy Bypass -File "D:\IndustrialTechnology\industrialMOD\performance-check-20261023\applied-renderer-backup\Undo-Renderer-Restoration.ps1"
```

The rollback validates paths and JAR hashes, moves the three restoration files into its disabled-restoration folder and returns the two previous dependency files. It does not edit any save. Raw reports and screenshots remain in the three clearly named diagnostic project folders.
