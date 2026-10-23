# Personal Factory Fix 0.2.1 comparison

The installed and built JAR SHA-256 is
`862E81DF09405465D670567F231E088648AF7A9BA7E95EB68E54B04F36E48B79`.

NVIDIA SMI reports RTX 5070 Laptop GPU, driver 617.42. Minecraft also reported
617.42 at startup. Minecraft 1.7.10 / Forge 10.13.4.1614 / Oracle Java 8u51.
No Angelica or OptiFine was installed for this comparison.

The player remained at the same view near the factory doorway for both captures;
no blocks or machine contents were edited. F10 disabled the filter, then F9
recorded 30 seconds. F10 enabled it, then F9 recorded another 30 seconds.

| Metric | Off: factoryprobe-1792684661457.txt | On: factoryprobe-1792684826930.txt |
| --- | ---: | ---: |
| Duration (seconds) | 30.01 | 30.00 |
| Frames | 1,164 | 1,865 |
| Average FPS | 38.8 | 62.2 |
| Invalidation requests | 26,690 | 7,018 |
| Eligible duct packets | Not counted while disabled | 9,590 |
| Identical duct descriptions | Not counted while disabled | 9,589 |
| Redraw calls skipped | 0 | 18,713 |
| Redraw calls accepted | Not counted while disabled | 467 |

Requests fell about 74%; average instrumented FPS rose about 60%. These are
sequential samples with ongoing factory activity, not repeated benchmarks.
Invalidation requests are not actual chunk rebuild counts. The diagnostic itself
has overhead that scales partly with invalidation count. No claim is made that
normal play everywhere gains the same amount or that the previous 120–150 FPS is
restored. Other invalidations, including HBM gas block changes, remained.

The filter was left enabled, the diagnostic capture finished, and the game paused.
Version 0.2.0 failed open because protected inherited world fields were not found;
0.2.1 corrects field lookup. The helper's protected inherited field fixture passed.
Transformer verification on the actual CoFH/TD classes passed before installation;
the transformer bytecode was unchanged between 0.2.0 and 0.2.1.
