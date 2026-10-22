# Verification — Industrial Technology 2.1.0

Java 8 / Forge 10.13.4.1614. The reports below are from performed runs; diagnostic failures and previous releases are not counted. Repeated compatibility assertions count as separate executions.

| Archived report | Passed checks |
|---|---:|
| space-standalone-initial.txt | 29 |
| space-standalone-restart.txt | 6 |
| space-hbm5771-initial.txt | 36 |
| space-hbm5771-restart.txt | 6 |
| upgrade-from-2.0.1.txt | 4 |
| shuttle-flight-client-hbm5771.txt | 24 |
| shuttle-gallery-client-hbm5771.txt | 9 |
| TEST-dev.industrialtechnology.automation.AutomationReferenceTest.xml | 2 |
| TEST-dev.industrialtechnology.automation.RepeatTimingTest.xml | 7 |
| TEST-dev.industrialtechnology.automation.ScriptCompilerTest.xml | 12 |
| TEST-dev.industrialtechnology.automation.ScriptPacketTest.xml | 5 |
| TEST-dev.industrialtechnology.client.CodeEditorModelTest.xml | 6 |
| TEST-dev.industrialtechnology.quarry.AreaCursorTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.BoundaryGeometryTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.MiningRateTest.xml | 3 |
| TEST-dev.industrialtechnology.space.LaunchFacilityPlanTest.xml | 2 |
| TEST-dev.industrialtechnology.space.PlanetTerrainTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.DeviceSearchTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.ReactorSettingsTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.WirelessCatalogTest.xml | 5 |
| TEST-dev.industrialtechnology.wireless.WirelessCommandTest.xml | 4 |
| Total recorded successful executions | 182 |

## Validated behavior

- Final reobfuscated JAR on dedicated Forge servers with and without the user's actual HBM X5771: original research, crafting, manufacturing, suits, air/pressure/leaks, legacy launchpads, lamp light and saved payload checks; new Shuttle assembly and geometry; full LC39 deployment, occupied/overlapping site recovery, real Forge placement/break protection, bounded chunk preparation and part batches, no per-part tile entities, partial build/recovery save/restart and exactly-one recovery kit with retained contents.
- Real HBM storage tank and two genuine MK2 pipe blocks supply kerosene and cryogenic oxygen through a native network. Source debit equals receiver credit. Tests cover per-tick budget, partial/full stores, disabled/protected receivers, invalid fluid/pressure, broken/unloaded connections and direct LC39 root subscription. The native tank's 500 mB provider rate limits a single test update below the receiver's 1,000 mB/t shared ceiling.
- Actual crafting-grid outputs for nine titanium/aluminium concentrate are **the exact HBM titanium/aluminium ingot items**. Both planets generate their own mineable minerals in new chunks; iron furnace output and finite ice-water crafting are checked.
- Real HBM + NEI development client: survival kit placement, full Earth -> Moon -> Earth -> Mars -> Earth flight, exact fuel burns, retained inventory/XP/equipment, finite canister refuelling, elevated lunar/Martian jumps, pressure protection and a wall breach. This flight completed before the final connected-brace/exhaust artwork and terrain-preparation changes. Common flight code was unchanged by those later changes; final construction and restart behavior was checked again in the packaged dedicated-server release.
- Separate focused client gallery after the final brace rendering: actual synchronized facility root/model and large structure, all new mineral blocks, facility controller and assembly-type GUI, NEI enabled, resource reload and OpenGL error/color checks. Captures were inspected. Test-only fixture ground is elevated in a disposable flat world; the kit itself builds directly above clicked ground in normal play.
- Upgrade from 2.0.1: load a copy of the prior disposable test save under the final JAR, verify legacy rocket/HDD, stored fuel, suit output, lamp light and planet save folder. Every original source-save hash remained unchanged. No human player's world was used.
- Release audit checks packaged Java 8 bytecode, absence of third-party/test classes and nested JARs, retention of all existing artwork/reference bytes and non-space subsystem bytecode, current source documentation, and exact JAR identity across production runtime copies.

## Limits

The full flight and visual checks use a development client; final packaged bytes are checked on native dedicated servers. These runs do not cover every GPU, shader/resource pack, mod version, claim plugin, multiplayer combination or extended survival session. Mineral grades/yields and Shuttle planetary travel/kerosene fuel/vertical stack recovery are documented gameplay abstractions. Geometry follows real principal dimensions with voxel detailing.

Old 2.0.1 evidence remains in `docs/TESTING-2.0.1.md` and its versioned evidence directory. Existing installed Minecraft mods and user saves were not modified. Install `build/libs/IndustrialTechnology-1.7.10-2.1.0.jar` by replacing the older IndustrialTechnology JAR while keeping existing config and saves. See `SPACE_EXPLORATION_GUIDE.md` and `SPACE_HANDOFF.md` for current operation and handoff.
