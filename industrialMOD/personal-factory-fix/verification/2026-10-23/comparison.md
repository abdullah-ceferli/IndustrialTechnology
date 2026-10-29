# Without-Angelica captures and diagnostic update

The captures used PersonalFactoryFix 0.2.2 with its plain-itemduct filter enabled.
They were recorded in order after the user was asked to record a smooth area and
then walk into the base. The reports do not contain player positions, so this is
an inferred labeling, not verified identical locations or a controlled benchmark.

| Measurement | 22:49 recording | 22:50 recording |
| --- | ---: | ---: |
| Duration | 30.00 s | 30.02 s |
| Average FPS | 54.4 | 46.6 |
| Median frame interval | 16.999 ms | 19.975 ms |
| 99th percentile interval | 43.440 ms | 48.726 ms |
| Worst interval | 150.560 ms | 79.316 ms |
| Render invalidation requests | 71,236 | 12,707 |
| GC collection-time delta | 270 ms | 164 ms |

The slower recording had fewer invalidation requests, so request count alone does
not explain performance. Requests are not completed mesh rebuilds. The two
captures' average FPS also does not reproduce a sustained 20–40 FPS observation.

Special tile renderer callbacks in the second recording used about 2.05 seconds in
total, or 1.47 ms/frame. Ender itemduct rendering accounted for 905 ms total, or
0.65 ms/frame. These are CPU/driver elapsed times, not GPU execution measurements;
they cannot rule out a rendering bottleneck. Some slow frames overlap GC activity,
but GC alone does not account for the sustained frame times. No root cause or FPS
fix has yet been established.

The current launch log reports RTX 5070 Laptop GPU, NVIDIA 617.42 and Java 8u51.
Options report render distance 16, maxFps 120, VSync false. This is not evidence of
the game being capped at 20–40 FPS.

PersonalFactoryFix 0.2.3 adds manual-F9 elapsed-time scopes for client ticks,
camera/world/GUI rendering, vanilla section rebuilds, terrain, entity/tile rendering,
the local server tick, display updates, FPS limiter waits and GL flushes. It also
records per-second location/facing samples and the slowest frames. Scopes are
nested, server work overlaps client work, and frame intervals are measured between
RenderTick.END events; do not sum scope times or treat correlations as proof.

The existing duct-filter and loading-plugin class files are byte-identical to
0.2.2. No tick scheduling, chunk submission, machine logic or world data changes
are part of this update. It is a diagnostic update, not a demonstrated FPS fix.

Validation passed:

- All new timing hooks on five real Minecraft classes in MCP and SRG namespaces.
- ASM analysis of the transformed classes, including existing return paths.
- Existing vanilla, tile-render and CoFH/Thermal Dynamics hook checks.
- Executed transformed-tick fixture with JVM bytecode verification enabled.
- Nested, inactive, thread-filtering, reset and aborted-client-frame recovery checks.
- Existing frame percentile and correlation report checks.

See `timing-hook-checks.txt` for results. The new JAR has not been live-tested yet.

JAR: `personal-factory-fix/build/PersonalFactoryFix-0.2.3.jar`

SHA-256: `145133B41A5F498575183D3447E06292A380516BCF0D9257EADCEBA9B0A2DED1`
