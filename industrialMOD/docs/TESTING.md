# Verification record — Industrial Technology 1.3.0

## Environment and release

Windows 11 x64; Temurin OpenJDK `1.8.0_504-b01`; Gradle 7.4.2;
ForgeGradle maintenance fork `1.2-1.1.1`; Minecraft 1.7.10;
Forge **10.13.4.1614**. Java compilation, tests, resource processing,
sources packaging, and release reobfuscation completed successfully.

The installable archive is `build/libs/IndustrialTechnology-1.7.10-1.3.0.jar`.
It was loaded outside the development launcher, with the official Forge
universal JAR and the vanilla obfuscated Minecraft server. The final archive
payload is checked against that production-tested JAR; see
`test-results/release-audit.txt` and `test-results/release-sha256.txt`.

## Recorded results

| Suite | Passed | Failed | Evidence |
| --- | ---: | ---: | --- |
| Geometry/cursor JUnit tests | 5 | 0 | `test-results/unit-tests.xml` |
| Mining-rate and measured-speed JUnit tests | 3 | 0 | `test-results/mining-rate-tests.xml` |
| Sparse boundary geometry and legal-height JUnit tests | 5 | 0 | `test-results/boundary-tests.xml` |
| Real dedicated-server world tests, development runtime, without HBM | 53 | 0 | `test-results/integration-initial.txt` |
| Actual saved-world server restart, development runtime | 4 | 0 | `test-results/integration-restart.txt` |
| Actual client GUI, world rendering, and packet/inventory tests | 52 | 0 | `test-results/client-integration.txt` |
| Release JAR world tests, ordinary Forge runtime with actual HBM | 61 | 0 | `test-results/production-initial.txt` |
| Release JAR saved-world restart, ordinary Forge runtime | 4 | 0 | `test-results/production-restart.txt` |

The recorded runs cover **130 distinct checks**, with **187 successful
checks across the recorded runs** when the production repeats are included.
The separate test harness is excluded from the installable mod.

## Generic ore compatibility and actual HBM

The release supports Forge harvest contracts without a mod-name allowlist.
The original drill's ItemTool fallback could not harvest ordinary modded rock
ores without an explicit Forge tool declaration. It now inherits ItemPickaxe's
fallback, while the internal quarry stack meets the target's declared tool
class and harvest level. The separately craftable hand drill stays level 4.
Test fixtures exercise undeclared tools, harvest level 12, and a custom
`drill` class at level 9.

Ore Dictionary registrations identify natural ore TileEntities, including
ores whose resource variant lives in NBT rather than world metadata. Their
resource data remains available during drop preparation. Inventory, RF, fluid,
and technology machine TileEntities stay protected even if registered as ores.
Changing an ore TileEntity while output is full invalidates its cached drops.

The user's actual `HBM-NTM-.1.0.27_X5808.jar` was copied into a fresh disposable
production test server; its original file and modpack were not modified. The
release JAR and separately reobfuscated harness were loaded by the official
Forge universal launcher with vanilla obfuscated Minecraft, without Gradle.
Uranium, thorium, copper, lead, titanium, fluorite, niter, and sulfur each passed
actual Fortune III and supported Silk Touch harvesting, output, and RF checks.
The release contains no HBM dependency or HBM classes.

HBM uranium replaces itself with radon gas during its ordinary drop callback.
The quarry now records this target replacement, restores the original ore while
checking output capacity, then applies the replacement after successful removal.
A separate self-replacing ore fixture verifies that full output preserves the
original ore and RF, and later commits its drops and replacement exactly once.
Ore TileEntity replacements are unsupported and pause the quarry.

The full ordinary Forge/HBM run passed 61 checks, followed by four checks after
an actual saved-world restart. A fresh development server without HBM passed
53 checks and the same four restart checks. Configuration tests verify automatic
migration of previous defaults, preservation of differing custom values, and
that migration does not overwrite subsequent user edits.

## Protection and Void Filter

A real quarry at Y=10 removes sentinel stone at Y=9 in all 25 selected chunks
while preserving the Y=10 and Y=11 sentinel blocks. Adjacent chest contents and
supported redstone wire remain intact. Separate checks protect inventory and
non-inventory TileEntities below the quarry, validate registry-identity block
exclusions, and clamp old/corrupt cursors below the placement layer.

Filter checks verify the default OFF state, cobblestone deletion, preservation
of nonmatching Fortune diamonds, real Silk drops, exact metadata matching,
metadata-insensitive matching with strict item/NBT identity, and all 18 icons
round-tripping through NBT. Full output permits an all-filtered harvest and
prevents removal or RF charging for nonmatching drops that cannot fit.
Event-modified oversized drops wait while the original block remains intact,
then commit exactly once after enough output is extracted.

Server ghost-click checks verify that icons have count one while the real
carried stack remains unchanged, right-click clears only configuration, hidden
and shift-click ghost mutations are rejected, and oversized NBT icons are
rejected. Real client window-click packets also configure and clear the icons
on all three tiers, with exact player item counts preserved. Auto Delete,
matching mode, and Clear All synchronize correctly.

## Public rendering, crystal, and stable colors

The real client receives enabled state, sparse selections, legal mining height,
and actual forced masks through vanilla world description packets. This uses
the chunk watcher path and does not depend on GUI snapshots or machine owner.
All three quarry frames appear on Start and disappear on Stop. Loader geometry
is checked for 1, 15, 25, and separated irregular chunks, including internal
borders and local hide/show without altering server tickets.

The client moves outside the original watched chunks and returns while the
machines remain enabled. The public loader mask and overlays reappear without
opening any GUI. World geometry is deduplicated, cached outside the per-frame
render path, range-limited, and camera-relative.

Actual framebuffer screenshots show active red frames, cyan crystal facets,
later active rotation, and the stationary OFF crystal. A pixel test checks cyan
facets in the real world framebuffer, excluding the block's cyan corners and
sky. Animation calculations check the fixed OFF pose and the five-second
partial-tick ON rotation. Screenshot inspection caught an initial dark-crystal
lightmap issue; the final renderer disables the multiplicative lightmap for
its untextured facets and restores texture/OpenGL state afterward.

GUIs run at 960 × 720 with GUI scale 3. Tests inject incoming OpenGL tint,
compare fixed panel and slot-rim pixels across modes/tabs/states, and verify
the caller's color is restored. The new Void Filter retains the existing
palette. Vanilla inventory pixels also match before and after the world
boundary/crystal renderers. Screenshots in `screenshots/` were visually inspected.

## Existing behavior and measured mining rates

Existing checks cover all Fortune tiers, Silk, metadata, bedrock and protected
events, RF reception/simulation and sided limits, safe output/extraction,
1–25 sparse selection, ticket failures/cleanup, completion, recipes, NBT, and
fair shared operation budgets. Inventory TileEntities are now protected,
replacing the older chest-harvesting behavior deliberately.

Eight quarries run simultaneously in a dedicated server with no players:
40 warmup ticks followed by 200 measurement ticks, default 4,000 RF/block and
five removals per machine/tick. These are game-time rates at **20 TPS**, rather
than a wall-clock benchmark of a large modpack.

| Quarry | RF/t | Measured blocks/s |
| --- | ---: | ---: |
| MK3 | 200 | 1 |
| MK3 | 2,000 | 10 |
| MK3 | 5,000 | 25 |
| MK3 | 10,000 | 50 |
| MK3 | 15,000 | 75 |
| MK3 | 20,000 | 100 |
| MK1 | 20,000 | 100 |
| MK2 | 20,000 | 100 |

These tests check actual mined counts, measured GUI speeds, RF ledgers, and
operation limits. An idle full buffer also reaches the configured rate with
an RF supply connected. One 200-RF packet cannot pay for a 4,000-RF block.
Stop preserves inventory/cursors with RF connected, and Start resumes.
Restarting the actual saved world without players preserves
the quarry's legal height and Void Filter, RF, mode, items and cursors; active
machines restore exact 15-chunk sets and disabled machines remain off. Breaking
them releases all their owned tickets. The loader has no RF/inventory APIs.

## Remaining limitations and coverage

Thermal Expansion, Thermal Dynamics, BuildCraft, NEI, and third-party
claim/protection mods were unavailable. Their actual power cables, converters,
pipes, and permissions are **unverified**. The CoFH RF contract, sided output
inventory contract, Forge event cancellation, and GUI interaction checks were
tested directly. HBM ore harvesting was tested with the version above; its
power cables, HE converters, machines, and full modpack behavior were not tested.
Direct HBM HE support is not implemented. Generic compatibility does not mean
every release of every mod has been tested; unbreakable deposits, protection
events, and mod-specific hardcoded restrictions still apply.

The protection fixture uses a vanilla chest/inventory and supported redstone
wire as infrastructure stand-ins; it does not claim a real third-party battery
or RF cable was installed. Natural registered ore TileEntities are allowed;
machine interfaces and other TileEntities remain protected. Actual external mod
callbacks that alter unrelated world positions cannot be rolled back by the
target transaction and may require explicit exclusions. Harvest staging is
verified for vanilla blocks, synchronous Forge drop events, ore TileEntities,
and HBM uranium's target replacement; asynchronous mod harvesters were not
tested. Uncommitted cached drop plans are recalculated after a restart;
the original block remains intact, while owned output is saved normally.

No simultaneous multi-client remote session, large-modpack soak test, or FPS/TPS
benchmark was run. Public synchronization was exercised with a real non-owner
client and real chunk-watch reloads in an integrated server, plus dedicated
server production loading and restarts. The ordinary Forge watcher broadcast
is used for other nearby clients; no owner-only rendering channel is used.

All test worlds are disposable `run-test-*` folders. Logs remain in `.toolchain`.
The recorded 1.3.0 worlds are `run-test-20261014-151152`,
`run-test-client-1.3.0-verified`, and `run-test-production-1.3.0-hbm-verified`.
