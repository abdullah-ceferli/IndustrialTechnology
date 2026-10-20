# Verification record — Industrial Technology 1.5.0

Minecraft 1.7.10, Forge **10.13.4.1614**, Windows 11 x64, Temurin Java
**1.8.0_504-b01**, Gradle 7.4.2 and ForgeGradle maintenance fork 1.2-1.1.1.
`tools/build.ps1 -Tasks @('test','build')` compiles, tests, packages sources,
checks the shared-reference hash and reobfuscates the installable release.
The JAR is `build/libs/IndustrialTechnology-1.7.10-1.5.0.jar`.

Historical records are preserved in [TESTING-1.4.1.md](TESTING-1.4.1.md),
[TESTING-1.4.0.md](TESTING-1.4.0.md), and [TESTING-1.3.0.md](TESTING-1.3.0.md).

## Recorded results

All final portable reports are in [test-results/1.5.0](test-results/1.5.0/).

| Suite | Passed | Failed | Evidence |
| --- | ---: | ---: | --- |
| Cursor geometry JUnit | 5 | 0 | `unit-tests.xml` |
| Sparse boundary and legal height JUnit | 5 | 0 | `boundary-tests.xml` |
| Fractional mining rates JUnit | 3 | 0 | `mining-rate-tests.xml` |
| Existing bounded command packets JUnit | 4 | 0 | `wireless-packet-tests.xml` |
| Restricted language parser JUnit | 9 | 0 | `script-parser-tests.xml` |
| Multiline editing state JUnit | 6 | 0 | `editor-tests.xml` |
| Script upload/document bounds and malformed UTF-8 JUnit | 5 | 0 | `script-packet-tests.xml` |
| Shared documentation/grammar coverage JUnit | 2 | 0 | `reference-tests.xml` |
| Native release Forge server with actual HBM X5808 | 122 | 0 | `production-hbm-initial.txt` |
| Actual HBM saved-world restart | 9 | 0 | `production-hbm-restart.txt` |
| Independent native release Forge server without HBM | 105 | 0 | `production-initial.txt` |
| Actual standalone saved-world restart | 9 | 0 | `production-restart.txt` |
| Real client quarry/filter/loader/rendering regressions | 56 | 0 | `client-integration.txt` |
| Real client wireless/placement/old automation regressions | 19 | 0 | `wireless-client-integration.txt` |
| Real client CODE editor/fluid/item/book tests | 23 | 0 | `code-client-integration.txt` |

There are **382 successful checks across these recorded runs**, including 114
native server/restart repeats without HBM; excluding that repeated suite leaves
**268 distinct checks**. No test harness classes or external mod JARs are shipped.

Both native servers load the actual reobfuscated release using the official
Forge universal launcher and vanilla obfuscated Minecraft server, outside
Gradle. The final archive is rebuilt after development launches, then every ZIP
entry is compared byte for byte against both production-tested copies (ignoring
archive timestamps). The audit checks Java 8 class versions, release metadata,
37 textures, reference/Markdown consistency, licenses, and recorded pass counts.
See [release-audit.txt](test-results/1.5.0/release-audit.txt) and
[release-sha256.txt](test-results/1.5.0/release-sha256.txt).

## Real CODE editor and guide book

The client opens the existing terminal, chooses a physical Blue tank torch,
and selects CODE. Old condition/item/amount/target/rule widgets disappear.
Player inventory and ghost slots are hidden and cannot be edited in CODE.
The right side contains a genuine multiline widget with Save, Help, and Enable.

Actual GUI input exercises typing, Enter, arrows, Home/End, Backspace, Delete,
mouse cursor placement/drag selection, Ctrl+A/C/X/V, scrolling, line numbers,
and real clipboard contents. Ctrl key state is injected into LWJGL's input
buffer; no external desktop mouse/keyboard automation is claimed. Separate
unit tests exercise multiline selection replacement, line-boundary deletion,
vertical desired-column behavior, bounds, CRLF normalization, and undo/redo.
Normal editor typing is consumed before container hotbar/game keys.

Save sends the real bounded FML upload and receives a revision acknowledgment.
An unknown-fluid draft shows Line 1, stays editable, and leaves the old working
program running. Saving unchanged code preserves its active pulse deadline and
edge latch. Device switching restores independent programs and SIMPLE settings.
Help is an internal classic-book overlay; closing it preserves the draft and
the live terminal container. Book right-click, page arrows, and Done also work
from the actual held guide item. Every generated book page is measured with the
real Minecraft font: at most 13 lines and 116 pixels per line.

Actual framebuffer screenshots of CODE, syntax colors, errors, scrolling,
fluid/item programs, editor Help and the held guide were saved and visually
inspected. The book uses the runtime vanilla parchment texture and original
brown leather item artwork. Incoming OpenGL tint and vanilla inventory colors
remain stable. Evidence is in [screenshots/1.5.0](screenshots/1.5.0/).

The native crafting manager confirms the shapeless Book + Redstone + Paper
recipe gives exactly one Automation Guide. The guide, editor Help, and complete
root [AUTOMATION_CODES.md](../AUTOMATION_CODES.md) use one JSON reference. Tests
require every condition/action enum to be represented and compile every actual
reference example. The build verifies the generated Markdown's source hash.
NEI was not installed; normal recipe registration is verified, NEI rendering is
not claimed.

## Inventory, fluid and redstone execution

The exact `WHEN FLUID "water" >= 200mb / THEN PULSE "Pump" 7s / END`
program is saved against a real physical Blue tank torch and authorized Yellow
torch. At 100 mB it remains false. Filling the actual Forge tank to 200 mB
starts power 15 and lights a vanilla lamp for exactly **140 server ticks**.
The signal stops automatically; stable true data never extends or retriggers
the pulse. A real chest insertion with the stone program similarly runs for
exactly **100 server ticks**. These are five/seven game-time seconds at 20 TPS,
rather than wall-clock timing guarantees on a lagging modpack.

Native tests also exercise actual item amounts, inventory empty/full, tank
empty/full, low-fluid comparisons, summed same-fluid quantities, ON/OFF,
configured-duration pulses, and unchanged saved mode/duration settings.
Multiple fluid conditions share one tank API scan per endpoint poll.
Simultaneous rising actions targeting one control collapse to the last script
block, preventing immediate contradictory signal oscillation. Active pulses
ignore more pulse requests; ON/OFF cancel the timer consistently.

Unavailable tank APIs do not become 0 mB, do not fire low-fluid conditions, and
do not clear a true latch. Unknown fluid/item names are Save errors. Failed
Save keeps source, compiled rules and edge state. Previously saved IDs are
validated once after load; missing IDs disable the program. Corrupt oversized
saved source and invalid compiled fields fail safely. Owner/network/dimension
checks, duplicate name rejection, explicit authorized UUID references, foreign
uploads, and absence of full source text from periodic snapshots are covered.

Offline targets remain unloaded; affected edges pause without being consumed.
The program resumes safely when its target becomes available. No scripts parse
every tick: successful Save compiles at most eight finite rules. Existing
SIMPLE rules and CODE rules remain saved separately, with only the selected
engine running. Actual restart preserves source, mode, enabled flag, compiled
bindings and latched truth, without a duplicate pulse.

## Actual HBM coverage and remaining machine limits

The existing user `HBM-NTM-.1.0.27_X5808.jar` is copied only into disposable
servers. No installed modpack, original HBM file, or existing player world is
modified. The same new fluid program reads **actual HBM Chemical Plant, Fluid
Tank, and Big Ass Tank classes**, sets their actual HBM tank from 100 to 200 mB,
and verifies a 140-tick deadline and real Yellow output. HBM fluid identifiers
are obtained from the inspected `Fluids.getAll()` / `FluidType.getName()` APIs;
unknown names never use HBM's default-fluid fallback.

Existing actual Arc Furnace inventory, Chemical Plant/tank monitoring, and
two proxy/controller tests still pass. Fluid-capability flags prevent power-only
ports exposing unrelated controller storage. Tank data uses public supported
API sets; an oversized, unresolved or partly unsupported set is unavailable.
Dedicated-server code uses common fluid IDs/translation keys and avoids the
HBM client-only localized-name method.

These controller/proxy fixtures use test-only host blocks. **Complete assembled
HBM multiblocks and dynamic Chemical Factory delegates were not exercised.**
Other HBM forks and individual machines' acceptance of vanilla redstone remain
unverified. Arc Furnace molten-material lists are not treated as fluid tanks.
No generic machine-control API or HE conversion is added. Unsupported machines
show DATA UNAVAILABLE/FLUID DATA UNAVAILABLE rather than fabricated quantities.

## Preserved regressions

Native tests still harvest eight actual HBM ores with Fortune and supported
Silk Touch; generic declared/undeclared tool classes, high harvest levels,
natural ore TileEntities and self-replacing uranium remain covered. Infrastructure
inventories/TileEntities and canceled Forge events remain protected. Staged
drop/output/RF transactions, completion, safe extraction, and mode persistence
continue to pass.

At 4,000 RF/block, eight simultaneous quarries retain measured game-time speeds
of 1/10/25/50/75/100 blocks/s at 200/2,000/5,000/10,000/15,000/20,000 RF/t for
MK3; MK1/MK2 also reach 100 blocks/s at 20,000 RF/t. These use 40 warmup ticks
then 200 measurement ticks, checking actual removed blocks and RF ledgers.

The 18/36/50 filters keep legacy NBT, real ghost item preservation, page-aware
edits, last slots and Clear All across pages. All 54 output slots remain real.
Physical colored torches still place through normal non-sneaking client
right-clicks, including ground placement before a terminal, later linking,
input/output faces, all six orientations, exactly-one consumption/drop,
permissions, occupied space, legacy virtual upgrades, and restart ownership.
Green remains read-only; Yellow manual controls and old SIMPLE rules work.

RF-free chunk loading, 1–25 sparse selections, exact restored forced chunks,
ticket cleanup, legal quarry height, red public frames, cyan crystal animation,
chunk-watch range reloads, and vanilla GUI color restoration all pass. The
new release loads on a dedicated server without client-class dependencies.

## Reproduce and coverage boundary

```powershell
.\tools\build.ps1 -Tasks @('test','build')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PcodeClient','-PtestRunDir=run-test-code-new')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PwirelessClient','-PtestRunDir=run-test-wireless-new')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PtestRunDir=run-test-client-new')
```

Use fresh disposable folders. For native production runs, first build the
separate reobfuscated harness with `testModJar`, `-PproductionTest`, and
`-PtestRunDir=run-test-production-1.5.0-hbm-verified`. Then run
`tools/verify-wireless-production.ps1 -TestName run-test-production-new -WithHBM`
or omit `-WithHBM` (choose another port for concurrent servers). The helper needs
the inspected local HBM JAR, official runtime/JDK and generated runtime classpath
described in its source. It rejects existing test worlds, binds to loopback,
and accepts the Minecraft EULA only for its disposable server.

Run `build` again after development clients/servers before distributing, then
`python tools/audit_release.py`. Final native evidence worlds are
`run-test-production-1.5.0-hbm-final2` and
`run-test-production-1.5.0-standalone-final2`; clients are
`run-test-code-client-1.5.0-final2`, `run-test-wireless-client-1.5.0-final`, and
`run-test-client-1.5.0-final`. Full logs remain under `.toolchain`.

No simultaneous two-client remote session, large-modpack soak, or FPS/TPS
benchmark was performed. Ownership is exercised with distinct fake players,
actual server-authorized packets, and a real integrated client. Third-party
claim/protection plugins, Thermal/BuildCraft power networks/pipes, NEI UI, and
HE converters remain unverified. Standard Forge inventory/fluid/RF/event
contracts are exercised directly. Generic compatibility cannot guarantee
hardcoded mod restrictions, opaque custom slot limits, or arbitrary asynchronous
harvest callbacks. Current scripts act only within an authorized loaded network
in the same dimension and cannot add capabilities absent from a machine API.
