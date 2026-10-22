# Industrial Technology 2.0.0 — current space expansion handoff

The new expansion is implemented inside the existing Forge 1.7.10 / Java 8 mod (`industrialtech`). Existing quarry, loader, wireless/CODE/EVERY and HBM reactor systems remain registered under their original names. Baseline 1.6.3 sources/artifact and earlier test evidence were retained. No existing user world was used, edited or deleted during this work.

## Implemented

- Eleven craftable rocket parts and a powered assembly station; practical original cylindrical rocket mesh, fins, nozzle, capsule and landing legs.
- Validated 7×7 pad/controller/ports, physical removable research HDD, adjacent telescope and retro CRT computer; exact 200-tick Moon / 6,000-tick Mars research.
- Distinct Forge kerosene, oxygen gas, LOX, crude oil and breathable air; powered manufacturing, real canister interactions, native HBM storage-tank fluid-port adapter.
- Server-controlled countdown, ignition, ascent, deferred dimension transfer, descent and grounded landing; finite fuel/cabin air and real return reserves, reusable owned vehicle, recovery NBT and identity ledger.
- Separate configurable Moon/Mars providers/biomes, deterministic original crater/maria/highland/channel/volcanic/ice terrain, bounded optional preprocessed height-reference PNG support, low gravity, day cycles, near-side Earth and regional Mars dust.
- Three four-piece pressure suits with finite NBT air, backpack/visor models, refilling, warnings, thermal wear and HUD.
- Cached bounded room scans, finite pressurizer gas including unpowered/paused upkeep and breathing, physical two-door fixed-chamber airlocks with pressure interlocks and lossy recovery.
- Default empty-dimension suspension for this mod's quarry work/RF/output/tickets; persistent bounded resumption index, player/travel exemptions; other mods' tickets untouched.
- Cheap independent green redstone lamp with actual 0/15 light and restart persistence.

## Where the code is

Common space code: `src/main/java/dev/industrialtechnology/space/`. Client GUI/models/sky/HUD: `src/main/java/dev/industrialtechnology/client/space/`. The native small-tank port resolver is `wireless/HbmFluidAccess.java`. Existing integration points changed are the bootstrap/proxies and existing machine/ticket/scheduler gates.

Test-only code is under `tools/space-tests/`, separate from the gameplay jar. Legacy integration tests remain under `src/testMod/`. Read `docs/TESTING.md`, the per-report counts under `docs/test-results/2.0.0/`, and `release-audit.txt` for performed verification. Failed diagnostic directories are retained and excluded from successful check totals.

## Important limits for the next prompt

Read `SPACE_EXPLORATION_GUIDE.md` for actual recipes, inventory slots, fuel amounts and the exact airlock layout. Read `SPACE_TECHNICAL_NOTES.md` before extending integration or terrain.

- Default terrain is NASA-informed procedural synthesis, not a bundled 1:1 NASA map. Optional scientific-data preprocessing is external; the runtime importer reads bounded prepared grayscale PNGs.
- HBM support is verified native AIR/OIL/KEROSENE/OXYGEN through the small storage tank/controller and its inspected fluid-capable ports. It is not universal native-pipe, portable gas-item or HE-to-RF conversion support. Standalone Forge-fluid/air manufacture remains available.
- The airlock has a fixed 1×3×2 interior and Z-axis doors. Room and scan limits are configurable.
- Gravity hooks support players and vanilla-derived animals; custom vehicles, dropped items and exotic physics are not globally rewritten.
- Validation includes native dedicated-server saves/restarts, exact HDD/manufacturing balances, complete planetary client round trips, helmet-off pressure/leaks, canister refuelling, HBM/NEI and legacy regressions. It is not a claim of exhaustive testing with two human clients or every mod/claim system.

The release is built under `build/libs/IndustrialTechnology-1.7.10-2.0.0.jar`. This development task does not replace the user's installed Minecraft mod jar or modify their existing saves. The new jar must replace the older Industrial Technology jar in the client/server `mods` folders when installed.
