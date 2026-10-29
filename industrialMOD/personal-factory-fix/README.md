# Personal HBM factory patch investigation

Installed and current source: **0.2.4**, with Angelica removed at the user's request.
The later gas-redraw experiment was not installed; its source, tests and candidate
JAR are archived in `experimental/gas-filter-0.2.5/`. Read-only saved geometry showed
only 1 of 176 local radon/asbestos blocks met its conservative clear-space guard,
so there is little demonstrated opportunity for that candidate to help this base.

Version 0.2.4 extends the experimental redundant-redraw filter to exact plain and
Ender Thermal Dynamics 1.2.1 itemduct types, plus the diagnostic companion. A stationary 30-second
comparison in the affected world measured 38.8 FPS with the filter off and 62.2 FPS
with it on. This is one instrumented comparison, not a general FPS guarantee or a
complete fix for the reported regression. Version 0.2.0 failed open in production because its field lookup did
not find the inherited, protected world reference; 0.2.1 corrects that lookup.
It supports Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8 and does not require Angelica.
It always processes packet data, but suppresses duplicate client redraw requests
when the entire received duct description is identical to the previous description
for the same tile instance, world and coordinates. Changed descriptions redraw immediately. Identical data
still forces a redraw at least once per second. Server logic and other tile types
are unchanged. It never removes blocks or changes machine contents. F10 toggles the
redraw filter for comparison; F9 records a diagnostic capture. Install only one
Factory Diagnostics/Factory Fix JAR at a time.

Version 0.2.4 also invalidates saved full-description comparisons whenever either
supported duct receives a separate info packet. Ender power updates use that
channel, so comparing only against an old full description would be unsafe. All
packet handlers still execute normally, including item transfers and power updates.
The cache uses weak references to the tile's world, so it cannot keep a world and
its tiles alive through the weak-key cache. Unrecognized subclasses and unsupported
streams keep their original redraws. F9 reports an Ender-only subset of packet and
redraw counters so the benefit can be checked in-game. No FPS gain is claimed yet.

Version 0.2.3 added diagnostic timing scopes for the vanilla renderer: client ticks,
camera/world/GUI rendering, section rebuilds, terrain drawing, entity/tile rendering,
the local server tick, display updates, the FPS limiter, and the OpenGL flush. Reports
include a per-second location/facing timeline and the slowest frame intervals. It
changes no rendering decisions or tick scheduling, and does not claim an FPS gain.
The new scopes do no timing or location lookup outside a manual F9 capture. The
duct filter and loading plugin classes in that diagnostics-only release were
byte-identical to 0.2.2. Version 0.2.4 keeps those timing helpers unchanged.

The first 0.2.3 recording averaged 33.3 FPS. Camera/world/GUI rendering used
22.48 ms/frame, including 7.32 ms/frame in vanilla section rebuild calls, while the
local server completed 600 ticks in 30.04 seconds. Several long frame intervals
included 50–76 ms of rebuild work. The largest spike also overlapped GC. This
capture began about six seconds after joining, so warm-up and loading are factors;
the result identifies measured costs during the run, not the original regression.

The 0.2.4 recording averaged 44.1 FPS, but included about five seconds paused and a
different route/view. It recorded zero Ender description packets, so it does not
demonstrate an FPS gain from the Ender extension. Section rebuilds used 7.69 seconds
in total versus 7.33 seconds in the prior run. Their cost per frame was lower with
more frames rendered; this must not be called less total rebuild work. See
`verification/2026-10-23/after-0.2.4.md` for the comparison and pending graphics test.

The two 2026-10-23 captures without Angelica averaged 54.4 and 46.6 FPS. The slower
capture had fewer redraw requests (12,707 versus 71,236). Recorded special tile
renderers used about 2.05 seconds of 30 seconds in the slower capture. These CPU/
driver elapsed times alone do not explain the remaining frame time; terrain draw,
rebuilds, other client work and GPU execution were not measured by 0.2.2. GC
collection-time deltas coincided with some spikes but do not establish the cause of
the sustained low FPS. Capture overhead and different locations/activity prevent
treating these recordings as a controlled benchmark.

Profiling starts only when you press **F9**; no automatic capture runs during normal play.
In a loaded world, press **F9** and reproduce the problem for 30 seconds by running
through the base. Block changes are optional; walking is sufficient for the first
capture. A report appears in the game's
`logs/factoryprobe-<timestamp>.txt`. With Angelica personal-hbm5, it includes frame
percentiles and slow-frame upload, model-discovery, fallback and GC correlations.
The actual Angelica rebuild-request path is sampled too. Version 0.2.4 extends the
previous plain-duct filter as described above.
F9 again ends capture early. Capture includes loading/menu/pause time if used there;
only record in the world. The diagnostic sampling itself adds overhead, so FPS is
not an uninstrumented benchmark. Renderer timings include CPU work and driver waits.
Timing scopes are nested and must not be added together. GPU execution is not
measured. Local-server work overlaps client work and is unavailable for a remote
server. Counts are method calls, not necessarily completed chunk meshes. Location
samples belong to frame boundaries or the start of a timeline bin; they are not
proof of which block caused work. Partial first/last intervals and bins are possible.
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
