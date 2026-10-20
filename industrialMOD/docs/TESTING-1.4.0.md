# Verification record — Industrial Technology 1.4.0

The Java 8 / Minecraft 1.7.10 / Forge **10.13.4.1614** release builds successfully.
The installable file is `build/libs/IndustrialTechnology-1.7.10-1.4.0.jar`.
Testing used Windows 11 x64, Temurin 1.8.0_504-b01, Gradle 7.4.2, and the
ForgeGradle maintenance fork 1.2-1.1.1. The previous verification record is
preserved in [TESTING-1.3.0.md](TESTING-1.3.0.md).

> Historical record: these placement tests sent packets directly and missed
> a client right-click early return. Version 1.4.1 corrects that bug and tests
> the actual Minecraft client controller; see [TESTING.md](TESTING.md).

## Recorded results

All evidence below is in [test-results/1.4.0](test-results/1.4.0/).

| Suite | Passed | Failed | Evidence |
| --- | ---: | ---: | --- |
| Cursor geometry JUnit | 5 | 0 | `unit-tests.xml` |
| Sparse boundary and legal height JUnit | 5 | 0 | `boundary-tests.xml` |
| Fractional mining rate JUnit | 3 | 0 | `mining-rate-tests.xml` |
| Bounded wireless packet decoding JUnit | 4 | 0 | `wireless-packet-tests.xml` |
| Native Forge release server with actual HBM X5808 | 88 | 0 | `production-hbm-initial.txt` |
| That server's actual saved-world restart | 7 | 0 | `production-hbm-restart.txt` |
| Separate native Forge release server without HBM | 74 | 0 | `production-initial.txt` |
| Standalone saved-world restart | 7 | 0 | `production-restart.txt` |
| Real client quarry, loader, GUI and rendering | 56 | 0 | `client-integration.txt` |
| Real client wireless GUI, packets, automation and torch rendering | 16 | 0 | `wireless-client-integration.txt` |

These runs contain **265 successful checks**, including the 81 repeated native
server/restart checks without HBM; there are **184 distinct checks**. The two
production servers use the official Forge universal launcher and vanilla
obfuscated Minecraft server, outside Gradle. A separate reobfuscated test
harness is installed only in disposable worlds and is excluded from the mod.

The final archive is built again after development client runs. Its code and runtime
assets are compared byte for byte against both production-tested copies,
ignoring archive timestamps. The artwork attribution notice was updated after
runtime checks; the audit permits only that exact non-runtime text change.
Java 8 bytecode, metadata, all 34 textures,
license notices, test counts, and absence of bundled external mods/test classes
are checked by [tools/audit_release.py](../tools/audit_release.py).
See [release-audit.txt](test-results/1.4.0/release-audit.txt) and
[release-sha256.txt](test-results/1.4.0/release-sha256.txt).

## Wireless behavior exercised

Native server checks bind independent terminal networks and reject foreign
owners, stolen terminals, and foreign endpoints. Green/Yellow/Blue registration,
all four crafting recipes, selected faces, live chest and Forge tank reads,
unsupported APIs, and replaced-target identity are exercised. Offline and
cross-dimension requests leave chunks and dimensions unloaded. Read-only data
does not transfer items. Ghost edits preserve real carried items and accept
only linked Yellow targets.

Real Yellow relays emit weak power 15 and attached-face strong power 15, light
a vanilla lamp, and use separate 100–200-tick pulse deadlines. An actual chest
insertion triggers seven seconds of output (140 server ticks at 20 TPS) through
a Blue rule. Keeping the condition true does not retrigger it. Multiple rules
share an inventory scan and can select different relays. Automation runs with
no GUI or online player. Restart checks preserve networks, names, target anchor
identities, eight rules, edge latches, and seven-second pulse configuration.
Anchor persistence uses this mod's chunk-data key and leaves foreign machine
NBT unchanged.

The wireless client exercises the actual mouse event and FML command path:
left-click is canceled before attack/block breaking and opens the terminal.
The automated test injects the Forge mouse event, rather than operating a
physical mouse. Right-click fallback and vanilla item-use packets register all
three devices. Real GUI actions rename a target, switch a lamp on, choose pulse
settings, save an item ghost and target, and trigger real inventory automation.
The cursor and inventory item totals remain exact. An unloaded remote target
reports OFFLINE and remains unloaded; a loaded target works beyond ordinary
block interaction distance. Incoming OpenGL tint and vanilla inventory colors
are preserved across terminal tabs and world rendering.

All three inventory sticks use the same redstone-torch silhouette with colored
tips. The Yellow world relay uses the classic torch mesh. A real framebuffer
check sees the yellow cap and wooden stem; the saved screenshot was visually
inspected, including wall attachment and a powered lamp. Screenshots of monitor
items/tanks, controls, rules, and the torch are in
[screenshots/1.4.0](screenshots/1.4.0/).

## Actual HBM compatibility

The user's preserved `HBM-NTM-.1.0.27_X5808.jar` is copied into a disposable
native Forge server. Its original file/modpack is unchanged. Eight real ores
(uranium, thorium, copper, lead, titanium, fluorite, niter, sulfur) pass Fortune
and supported Silk Touch harvest, RF, and output checks. Uranium's self-replacing
drop callback is handled transactionally. Generic tool classes, high harvest
levels, and natural Ore Dictionary TileEntities remain covered.

Monitoring tests instantiate the actual X5808 Arc Furnace Large, Chemical
Plant, Fluid Tank, and Big Ass Tank classes and read their real inventories or
HBM tank objects. Two actual multiblock proxy tests resolve loaded Arc Furnace
and Chemical Plant controllers. The X5808 inventory/fluid capability flags are
respected, including rejection of power-only ports. Dedicated-server tank reads
use common fluid IDs/translation keys and avoid HBM's client-only localized-name
method. These controller/proxy fixtures use a test-only host block; they do
**not** claim to have built the complete multiblock structures in a modpack.

HBM remains optional: the same release passes an independent native Forge
installation without it. No HBM classes or JARs are bundled. Other HBM forks,
fully assembled multiblocks, dynamic Chemical Factory delegates, and individual
machines' response to vanilla redstone remain unverified. Arc Furnace molten
material lists are not reported as Forge tanks. Direct HBM HE power support is
unchanged and is not implemented.

## Quarry, filter, and loader regressions

The filter has exactly 18/36/50 logical slots for MK1/MK2/MK3. Old 18-slot NBT,
the final slot on each tier, exact metadata/NBT matching, Auto Delete OFF by
default, overflow behavior, and real carried-item preservation pass. Actual
client window-click packets configure the second pages on MK2/MK3, then Clear
All clears both pages. Output remains 54 real slots per quarry.

The existing protection, staged drop handling, Fortune/Silk, safe extraction,
shared operation budget, recipes, RF simulation, 1–25 sparse chunk selection,
ticket errors/cleanup, saved mining cursors, legal height, and restart checks
continue to pass. Inventory/machine TileEntities remain protected while natural
registered ores can be harvested. Genuine event cancellation is respected.

Sustained server tests run eight quarries together, with 40 warmup ticks and
200 measurement ticks, at 4,000 RF/block. The measured game-time rates at 20 TPS
remain:

| RF/t | MK3 blocks/second |
| ---: | ---: |
| 200 | 1 |
| 2,000 | 10 |
| 5,000 | 25 |
| 10,000 | 50 |
| 15,000 | 75 |
| 20,000 | 100 |

MK1 and MK2 also reach 100 blocks/second at 20,000 RF/t. These measurements check
mined counts, GUI rates, RF ledgers, and per-tick limits, rather than benchmarking
wall-clock modpack performance. Stop releases tickets and preserves inventory
and cursor while RF is attached; Start resumes normally.

Real client tests exercise public red boundaries, 1/15/25/irregular loader
selections, crystal animation, chunk-watch range reloads, GUI palette/tint, and
vanilla inventory color restoration. Actual screenshots retain visible cyan
crystal facets and red active boundaries. Loader operation remains RF-free.

## Reproduce and remaining coverage

Run `tools/build.ps1 -Tasks @('test','build')` for unit tests and the release.
For fresh real client worlds:

```powershell
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PtestRunDir=run-test-client-new')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PwirelessClient','-PtestRunDir=run-test-wireless-client-new')
```

The native production helper needs the official runtime/JDK, runtime classpath,
and preserved user HBM JAR described in its source. First build the separately
reobfuscated harness with `-PproductionTest`, `testModJar`, and
`-PtestRunDir=run-test-production-1.4.0-hbm-verified`. Then use
`tools/verify-wireless-production.ps1 -TestName run-test-production-new -WithHBM`
or omit `-WithHBM` for a standalone run. Use a fresh name; existing worlds are
rejected. Tests bind only to loopback and accept the Minecraft EULA for that
disposable server. Build again after client/server development launches before
distributing; then run `python tools/audit_release.py` against the recorded runs.

Thermal Expansion/Dynamics, BuildCraft, NEI integration, external power
converters/cables/pipes, and third-party claim/protection mods were unavailable.
Their actual integrations remain unverified; standard CoFH RF, Forge events,
and inventory contracts are exercised directly. No simultaneous multi-client
remote session, large-modpack soak test, or FPS/TPS benchmark was performed.
Public synchronization uses ordinary watcher broadcasts and was exercised by
a non-owner client and actual chunk-watch reloads in an integrated server.
Server ownership validation was separately tested with distinct fake players.

Opaque nonstandard slot limits, arbitrary external harvest callbacks that edit
other world positions, asynchronous harvesters, and hardcoded mod restrictions
cannot be guaranteed by generic contracts. Unsupported reads fail safely and
unbreakable/protected deposits remain protected. See [WIRELESS.md](WIRELESS.md)
for exact user behavior, bounds, polling, and unsupported APIs.

Disposable evidence worlds: `run-test-production-1.4.0-hbm-verified`,
`run-test-production-1.4.0-standalone-verified`, `run-test-client-1.4.0-verified`,
and `run-test-wireless-client-1.4.0-verified`. Full runtime logs remain in
`.toolchain`; portable reports and screenshots are preserved under `docs/`.
