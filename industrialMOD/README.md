# Industrial Technology — Minecraft 1.7.10

A Forge technology mod with three RF-powered quarry tiers and a separate,
precise chunk loader. Both share an extensible machine and ticket framework.

## Install

Use **Minecraft 1.7.10, Forge 10.13.4.1614, and Java 8**. Put
`IndustrialTechnology-1.7.10-1.5.1.jar` from `build/libs` into `mods` on both
the client and dedicated server. The sources JAR is for developers.
No Thermal Expansion, HBM, BuildCraft, or CoFHCore installation is required.
The small official CoFH RF API is bundled with its version annotation.

Quarries need an RF power source in survival. This mod does not yet add a
generator: connect an existing RF network, or a compatible HE-to-RF converter.
The standalone Chunk Loader always works without RF, HE, fuel, batteries, or any energy storage.

## Wireless monitoring and automation

Version **1.5.1** adds a left-click Wireless Terminal, Green Monitoring Sticks,
Yellow Redstone Control Sticks, and Blue Automation Sticks. The three sticks
use matching classic redstone-torch shapes, recolored green, yellow, and blue.
They share an owner-bound, world-saved registry and work without RF.

All three sticks place actual torches with a **normal right-click** on the
ground or a machine face; sneaking is optional. Carry your terminal to link
them automatically, or right-click an existing torch with the terminal later.
Green reads inventories/tanks behind its attached face, Yellow emits real
0/15 redstone, and Blue evaluates up to eight inventory rules. For
example, inserting stone into a linked chest can trigger a Yellow Stick's
seven-second pulse. Remote targets must already be loaded in the same dimension.

See [the wireless guide](docs/WIRELESS.md) for linking, controls, recipes,
compatibility, and automation behavior. The quarry speed remains **20,000 RF/t
for 100 blocks/s at 20 TPS**, with its existing ore/protection behavior.

## Automation Code Mode and guide book

In Automation, select a Blue Stick, choose **CODE**, write a program, **Save**,
then **Enable**. SIMPLE keeps the existing eight button-based rules. Only the
selected mode runs; both sets stay saved.

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "Pump" 7s
END
```

Rename a linked Yellow Stick to Pump in Controls first. A real water tank rising
through 200 mB starts seven seconds of redstone, then turns OFF. Stable true
conditions do not spam pulses. Item thresholds, inventory/tank empty/full, and
ON/OFF are also supported. Add `EVERY 20s` between WHEN and THEN to repeat while true;
`20m` means twenty minutes and `20h` means twenty hours. The first trigger is immediate.
False resets the timer; unchanged saves and restarts preserve its deadline. Invalid saves leave the working program unchanged.

The real multiline editor supports selection, copy/cut/paste, scrolling, line
numbers, syntax colors, and line errors. **Help** opens a classic cream-page
book while preserving the draft. Craft an **Automation Guide** with one Book,
one Redstone, and one Paper in any arrangement. Right-click it to read.

[AUTOMATION_CODES.md](AUTOMATION_CODES.md) is the complete beginner reference,
with every command and fifteen example projects. It is generated from the same
reference used by the book and Help. Rebuild it with
`python tools/create_automation_docs.py` after editing the reference; the build
checks its reference hash. The default script limit is 4096 characters, with
at most eight finite rules and no external scripting engine.

The verified optional HBM adapter reads actual Chemical Plant, Fluid Tank,
Big Ass Tank, and supported proxy data in X5808. Full assembled structures,
dynamic factory delegates, other HBM forks, and each machine's redstone response
remain unverified. Unsupported fluid data pauses rules rather than becoming
zero. Scripts cannot add redstone support to a machine that does not accept it.

## Machines and controls

| Machine | Fortune | RF capacity | RF per block | Maximum blocks/tick |
| --- | --- | ---: | ---: | ---: |
| Quarry MK1 | I | 400,000 | 4,000 | 5 |
| Quarry MK2 | II | 1,600,000 | 4,000 | 5 |
| Quarry MK3 | III | 6,400,000 | 4,000 | 5 |
| Chunk Loader | - | None | Always zero energy | Does not mine |

Right-click a machine to open its GUI. Machines start inactive, with only their
own chunk selected. Placement preserves the horizontal facing and records the
placing player's identity for protection checks.

The **Control** tab has three compact panels: status and energy, chunk map,
and harvesting/loading information. The **Output** tab exposes all 54 output
slots plus the full player inventory and hotbar. Shift-click extracts outputs.
Output slots accept no player or pipe insertion. Every side exposes all slots
for extraction through `IInventory`/`ISidedInventory`, including vanilla hoppers.
The **Void Filter** tab provides 18/36/50 ghost icons for MK1/MK2/MK3, with 25 icons per page and Auto Delete controls.
The Chunk Loader opens a separate smaller controls-only screen: no RF bar and no inventory.

Click cells in the 5 × 5 map to draft a selection. The center remains selected.
**1**, **3x3**, **All**, and **R** select the center, central nine chunks, all
permitted chunks, and reset back to center. **Apply** saves the draft on the
server. Hover cells for exact chunk and block coordinates; hover the count for
total area and the server limit. Negative coordinates use proper chunk flooring.

Blue-gray cells are selected, dark cells excluded, amber is the current quarry
chunk, and green marks completed chunks. The pale center marker locates the
machine. A thin amber line marks unsaved edits. **View/Preview** toggles applied
chunk outlines in the world, at the machine's upper face height. Click again to hide them; they also expire after 30 seconds.
The preview draws at most 25 rectangles and no particles.

**Fortune** and **Silk Touch** are mutually exclusive. The server supplies each
tier's Fortune level. Silk uses the real block's `canSilkHarvest` and
`harvestBlock` behavior, including modded harvesting events; unsupported blocks
use their ordinary behavior. The internal industrial drill supports Forge
pickaxe, axe, and shovel classes and adapts to each block's declared tool class
and harvest level. It also supplies Minecraft's pickaxe fallback for rock ores
whose mods do not declare a Forge tool. The craftable hand drill remains level 4.
This is generic compatibility, without a hardcoded list of supported mods.

Press **Start** to mine or **Stop** to suspend it and immediately release owned tickets.
A quarry begins at **its placement Y minus one**, then scans downward, Z rows
and X columns in every selected chunk. Its entire placement layer and all
higher layers are protected. The original height is saved; existing cursors
are clamped below it when loading older worlds. Above-height positions are
excluded in one operation rather than scanned every tick.

It skips air, bedrock, unbreakable blocks, protected TileEntities, and blocks
explicitly excluded through `MiningProtection.exclude(Block)`. Ore Dictionary
registered ore TileEntities can be mined with their resource data intact;
inventory, RF, and fluid-handler TileEntities remain protected even if registered
as ores. Other TileEntities remain excluded. This protects chests, energy cells,
generators, cables with TileEntities, other quarries, and chunk loaders even below
the placement layer. Ordinary fluids
with harvestable hardness can be removed. It respects world mining checks,
server spawn protection, and cancellable Forge `BlockEvent.BreakEvent` using
the placing player's FakePlayer profile. Denied blocks are skipped permanently
for that scan. There is no automatic retry of protected terrain.

Changes to the area preserve each chunk's existing cursor. Reselecting a
previously visited chunk resumes its saved progress. The scan finishes once
all selected cursors are complete; it does not continually rescan rebuilt terrain.
Breaking and placing a new quarry starts a new scan. Scanned percentage includes
air and protected/unbreakable positions; hover for actual mined/skipped counts.

## RF speed and safe output

The six sides share an aggregate **20,000 RF/t input ceiling** by default.
Simulation never consumes capacity or changes the reception counters. Quarries
receive RF but do not export it. Input is reported as a 20-tick accepted-RF
average. The default maximum-speed operating target is 20,000 RF/t, or 400,000 RF/s
at 20 TPS.

The default sustainable targets at 20 TPS are:

| Incoming RF/t | Maximum blocks/s | Average blocks/tick |
| ---: | ---: | ---: |
| 0 | 0 | 0 |
| 200 | 1 | 0.05 |
| 2,000 | 10 | 0.5 |
| 5,000 | 25 | 1.25 |
| 10,000 | 50 | 2.5 |
| 15,000 | 75 | 3.75 |
| 20,000 | 100 | 5 |

There is no fixed inter-block delay. A fractional work accumulator advances
according to RF rate, tier maximum speed, and speed multiplier. Every successful
block removal pays the full configured cost, **4,000 RF by default**. Credit is
capped at one tick's operation allowance. Stored RF cannot bypass per-machine
or global removal budgets. One 200-RF packet cannot pay for a default-cost block.
All three tiers have the same default speed; Fortune and RF capacity differ.

Input uses a 20-tick accepted-RF average. When storage is saturated, the machine
retains the measured supply rate only within one tick's RF reserve of full
storage, preventing a saturated buffer from causing a feedback stall. That
bounded reserve clears after the supply disconnects and the reserve drains.
Empty machines stop immediately when they cannot pay for a harvest.

The main GUI speed is **measured successful harvests over the last 20 game
ticks**, equivalent to one second at 20 TPS. The tooltip separately identifies
potential speed before shared server, terrain, and inventory limits.

The genuine Fortune/Silk harvest and Forge-modified drops are staged while
the target block is intact. The filter is applied, then all remaining drops
are checked against output capacity before removal. If they cannot fit, the
block and RF remain untouched and the status is **OUTPUT FULL**. The prepared
harvest is cached while waiting, avoiding repeated drop rolls. Extraction lets
the same prepared harvest proceed. Ore TileEntity data changes invalidate the
cached plan. Target replacements from non-TileEntity ore callbacks, such as HBM
uranium's radon gas, are restored during preparation and applied after successful
removal, so output backpressure keeps the original ore intact.

Unexpected extra drops from a custom removal callback are still captured in
the durable pending queue after a successful removal. Stacks are normalized
before saving so large drops do not overflow NBT's stack-size byte. Existing
pending output merges automatically and is never retroactively voided. No ongoing ground-item
spawning is used. XP is not farmed by quarrying. Breaking the machine releases
its tickets and drops its inventory and pending items normally.

Other statuses are **IDLE**, **MINING**, **STOPPED**, **NO POWER**, **COMPLETED**,
and **CHUNK LOADING ERROR**. Hover status for the exact ticket/harvest warning.
A failing third-party harvest handler pauses that machine and logs its location.

## Void Filter

Auto Delete starts **OFF**. Open **Void Filter**, pick up an item from your
player inventory, then left-click a ghost slot to copy its icon. Place the
real stack back into your inventory. The icon has a count of one and never
consumes, duplicates, or stores a real item. Right-click clears an entry;
**Clear All** clears every entry on both pages. MK1 has 18 entries, MK2 has 36, and MK3 has exactly 50. Previous/Next navigates pages; Configured reports the total across all pages. Existing saved entries keep their indices.

Enable **Auto Delete** to permanently delete matching newly mined drops.
**Exact Match** compares the item, metadata/damage, and NBT. **Ignore Metadata**
compares the item and NBT while allowing different metadata. Unrelated item
IDs and differently tagged stacks never match. Changing the filter does not
delete existing output or previously captured pending items.

A quarry can mine with full storage when every drop is filtered. It waits
without removing the block if any nonfiltered drops cannot fit. Ghost edits
use the authoritative server cursor stack and validated vanilla container
clicks; control packets contain only small action values. Entries are limited
to 1 KiB of serialized NBT each. Filter state and icons survive world saves.

## Public world boundaries and loader crystal

Nearby players see thin translucent **#FF3030 red** boundaries without opening
a GUI or owning the machine. Enabled quarries show the exact sparse perimeter,
with a frame at their placement Y and vertical edges down to Y=0. They never
draw a bounding rectangle across excluded chunks. Stopping a quarry hides its
active frame and preserves its selection.

Enabled chunk loaders show the outside and individual shared chunk borders
at the block's upper-face height. Their red grid represents only their
successfully forced chunk mask, not merely the requested selection. OFF and
ticket failures have no active loaded-chunk grid.

The quarry's **Area: ON/OFF** button and the loader's **Show Chunk Borders**
button toggle that kind of overlay locally for the current player. Preferences
persist in `config/industrialtech-client.cfg`; they do not alter server work
or tickets. The separate temporary Preview/View controls remain available.

Boundary state uses vanilla TileEntity description packets for chunk watches,
joins, reloads, and state changes. Geometry is cached, refreshed at most every
five client ticks, limited to nearby loaded machines, and deduplicated across
machines. Rendering uses camera-relative coordinates and restores OpenGL state.

A small original cyan crystal remains above every loader. OFF has a fixed
orientation and height. ON rotates once per 100 game ticks (five seconds at
20 TPS), with a subtle bob and partial-tick interpolation. The TESR renders
only the small crystal; large boundaries use the world-render event. Animation
is entirely client-side and sends no per-tick animation packets.

## Chunk loading

Running quarries force exactly the applied selection, including the machine
chunk, even while waiting for power or output space. Stopped and completed
quarries release their tickets immediately. An enabled Chunk Loader forces the applied
selection independently and never changes terrain. It always requires zero energy.
Its **ON/OFF** button immediately adds or removes its ticket contribution. OFF
is displayed as **DISABLED**; players and other loaders may still keep those
chunks physically loaded. The loader implements no RF or inventory interface.

Forge tickets contain machine coordinates and a persistent machine UUID. The
registered callback validates the live machine and enabled state during restart.
Multiple tickets are used if the server's depth is smaller than the selected
area. Every resulting forced set is checked against the desired set. Partial
allocation failure releases that machine's partial allocation and displays a
warning; retry occurs at most every ten seconds. It never alters another
machine's tickets. Breaking, disabling, changing selection, and chunk teardown
release or reconcile ownership correctly.

Server limits in `config/forgeChunkLoading.cfg` remain authoritative. For
example, `industrialtech` may use `maximumChunksPerTicket=25` and
`maximumTicketCount=200`. Zero disables the relevant capability. If automatic
quarry loading is turned off in the mod config, a quarry waits at an unloaded
target chunk instead of generating/loading it unconditionally.

## Crafting

All recipes are ordinary shaped crafting recipes and use vanilla ingredients.
NEI can display them without a custom recipe handler. Patterns are written from
top row to bottom row; a space is an empty slot.

| Output | Pattern | Keys |
| --- | --- | --- |
| 2 Control Circuits | `RGR / IPI / RGR` | R redstone, G gold ingot, I iron ingot, P paper |
| Machine Gear | ` I  / ISI /  I ` | I iron ingot, S stone |
| Diamond Industrial Drill | ` D  / DID /  R ` | D diamond, I iron ingot, R redstone |
| Quarry MK1 | `IGI / CPC / IRI` | I iron ingot, G Machine Gear, C Control Circuit, P diamond pickaxe, R redstone block |
| Quarry MK2 | `DGD / CQC / IRI` | D diamond, G Machine Gear, C Control Circuit, Q MK1, I iron block, R redstone block |
| Quarry MK3 | `EGE / DQD / CRC` | E emerald, G gold block, D diamond block, Q MK2, C Control Circuit, R redstone block |
| Chunk Loader | `IGI / CEC / IRI` | I iron ingot, G gold ingot, C Control Circuit, E ender pearl, R redstone |

The internal mining drill is supplied by the quarry; its standalone craftable
version is also a survival tool. All blocks and components appear in the
Industrial Technology creative tab. Tier upgrade recipes use the prior block.

## Configuration

The first launch writes `config/industrialtech.cfg`. Restart after editing. The `wireless` category controls endpoint/network limits and bounded automation polling; see `docs/WIRELESS.md`.

| Category | Settings |
| --- | --- |
| `general` | Maximum selectable chunks, automatic quarry loading, Fortune/Silk availability, debug logging |
| `energy` | Aggregate Quarry RF input cap and maximum-speed RF target |
| `quarrymk1`, `quarrymk2`, `quarrymk3` | RF capacity, RF per block, maximum blocks per tick, speed multiplier |
| `performance` | Cursor scans and real removals per machine/tick; global scan/removal budgets |
| `client` | Optional area preview |

Defaults: 256 cursor checks and five real removals per machine per tick, 4,096
global checks and 40 global removals per tick across all dimensions. A rotating
round-robin scheduler distributes work. Configuration values have finite bounds;
the chunk-selection upper bound is always 25. Inventory networking uses vanilla
changed-slot synchronization, and compact status snapshots go only to GUI viewers
at most once every five ticks, plus immediate control acknowledgments.

## Updating from 1.0.0 through 1.3.0

Replace the old mod JAR with 1.5.1; do not install both versions. Block and
TileEntity registration IDs remain unchanged. Stored RF, inventories, selected
areas, modes, and enabled state are preserved. Mining cursors below the
placement height are preserved; cursors above it are clamped to the first
legal layer. Quarries predating filters receive an empty, disabled Void Filter. Existing 1.2/1.3 entries remain in their original slots; the extra MK2/MK3 entries start empty. Partial legacy
work credit is normalized to the new block-credit format.

Previous default settings migrate automatically to 20,000 RF/t, 4,000 RF/block,
five removals per tick, and the expanded buffers shown above. Older fixed-delay
settings are removed. Values differing from previous defaults are preserved;
once migrated, later edits are not migrated again. A legacy custom value exactly
equal to an old default cannot be distinguished from that default. Old loader RF
settings are removed and ignored. There is no option to make the loader require energy.

Block artwork now follows the supplied references: gold top, turquoise corners,
and charcoal sides for the loader; silver monitor/control faces with orange or
amber framing for the three Quarry tiers. Original 32-pixel faces use classic
texture registration and ItemBlock inventory rendering.

The GUI uses fixed texture palettes. It resets color before textured drawing
and saves/restores custom OpenGL state, including client vertex arrays. State
changes affect compact controls, map cells, and energy fill; they never tint
the entire panel or inventory.

## Build with Java 8

The Gradle wrapper pins 7.4.2 and the compatible maintained ForgeGradle 1.2 fork
pins `1.2-1.1.1`. It retains the legacy 1.7.10 build/mapping pipeline, with working
HTTPS endpoints. See the [upstream build example](https://github.com/anatawa12/ForgeGradle-example).

Set `JAVA_HOME` to a Java 8 **JDK**, then run:

```powershell
.\tools\build.ps1 -Tasks setupDecompWorkspace
.\tools\build.ps1
```

This workspace also has a downloaded portable, checksum-verified Temurin Java 8
JDK under `.toolchain/jdk8`; the script uses it when present. The build cache is
project-local at `.gradle-user`. On other platforms use `./gradlew` with Java 8.

The reobfuscated installable JAR is `build/libs/IndustrialTechnology-1.7.10-1.5.1.jar`.
The `-sources.jar` contains the complete mod/API source and assets. **Run `build`
after `runClient`/`runServer` before distributing**: a development run can replace
the release archive with its unreobfuscated development input.

To regenerate original artwork, install Pillow and run `python tools/create_assets.py`
and `python tools/create_wireless_assets.py`.
Committed/generated PNG assets are sufficient for a normal Gradle build.

## Verification

`gradlew test` runs geometry/cursor, sparse boundary, fractional mining-rate, and wireless packet-bound tests. `tools/verify-server.ps1` creates
a fresh disposable server folder, runs the real world tests, restarts that world
to test persisted tickets, and checks both reports. It binds only to loopback,
uses port 25579, and accepts the Minecraft EULA for that disposable test server.
It does not run tests against an existing game save.

For actual client GUI tests, run:

```powershell
.\tools\build.ps1 -Tasks @('runClient', '-PtestClient', '-PtestRunDir=run-test-client-new')
```

Use a new test folder. The test client opens a 960 × 720 window, creates a
disposable integrated world, exercises the actual controls/packets/slots, saves
screenshots, and exits. The separate test harness JAR is never part of the
release mod. See `docs/TESTING.md` for actual results and remaining integrations.

## Development structure

`core` handles lifecycle, finite configuration, registries, and the server queue.
`machine` separates the common selection/ticket TileEntity from the optional
RF/output TileEntity; it also supplies containers, statuses,
and classic block rendering. `quarry` provides geometry, persistent per-chunk
cursors, protected mining, staged genuine harvesting, the Void Filter, capture,
and the bounded scheduler. `chunkloading`
shares the ticket manager with the standalone loader. `network` validates
window IDs, dimensions, distance, actions, and selection masks before server
mutations. `client` contains the pixel GUIs, cached public boundary renderer, loader crystal,
local overlay preferences, and temporary preview renderer;
dedicated servers use only the common proxy. `integration` currently provides
the Forge-compatible drill; RF interoperability uses the upstream API and item
interoperability uses standard inventories. New machines can extend `TileMachine`
and `BlockMachine` without copying the quarry engine or ticket lifecycle.

HE is not RF. Direct HBM HE support is deliberately not included without a
verified optional API. Use a compatible HE-to-RF converter. Eight actual HBM
1.0.27_X5808 ores were tested with Fortune and Silk Touch in an ordinary Forge
server. Actual X5808 inventory/tank controllers and two multiblock proxy types
also pass monitoring tests; full assembled structures remain unverified. See
`docs/WIRELESS.md` and `docs/TESTING.md`. Thermal Expansion/Dynamics, BuildCraft,
NEI, and other modpacks remain
unverified; the Forge RF and inventory contracts themselves are tested.

Harvest staging is verified with vanilla blocks, Forge drop events, ore
TileEntities, and self-replacing HBM uranium. Unsupported TileEntity replacement
callbacks pause the quarry. Arbitrary changes to other world positions or
asynchronous harvest callbacks cannot be rolled back by the target transaction
and may require explicit exclusions. Unbreakable deposits and mod-specific
hardcoded tool restrictions still apply. Actual third-party protection and
power-network modpacks remain subject to the coverage in `docs/TESTING.md`.
