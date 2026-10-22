# Verification — Industrial Technology 2.0.1

The visual release compiled successfully with Java 8 / Forge 10.13.4.1614. The 66 JUnit tests and all native reports below passed. Counts are recorded executions, including repeated checks across compatibility and close-up runs, not distinct features. Intermediate diagnostic runs are excluded and retained in their original temporary project directories.

| Archived report | Passed checks |
|---|---:|
| visual-client-hbm5771.txt | 213 |
| flight-client-hbm5771.txt | 21 |
| rocket-model-final.txt | 163 |
| space-standalone-initial.txt | 22 |
| space-standalone-restart.txt | 4 |
| space-hbm5771-initial.txt | 25 |
| space-hbm5771-restart.txt | 4 |
| upgrade-from-2.0.0.txt | 4 |
| TEST-dev.industrialtechnology.automation.AutomationReferenceTest.xml | 2 |
| TEST-dev.industrialtechnology.automation.RepeatTimingTest.xml | 7 |
| TEST-dev.industrialtechnology.automation.ScriptCompilerTest.xml | 12 |
| TEST-dev.industrialtechnology.automation.ScriptPacketTest.xml | 5 |
| TEST-dev.industrialtechnology.client.CodeEditorModelTest.xml | 6 |
| TEST-dev.industrialtechnology.quarry.AreaCursorTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.BoundaryGeometryTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.MiningRateTest.xml | 3 |
| TEST-dev.industrialtechnology.space.PlanetTerrainTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.DeviceSearchTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.ReactorSettingsTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.WirelessCatalogTest.xml | 5 |
| TEST-dev.industrialtechnology.wireless.WirelessCommandTest.xml | 4 |
| Total recorded successful executions | 522 |

## What was checked

- Actual HBM X5771 + NEI client: all 44 machine-face PNGs and six armor sheets load, metadata/placement matches panel direction, tile/inventory survives facing changes, all 11 server containers open, all space item/block icons render, suit tiers display from front/back and custom meshes render. Captures were visually inspected. The final focused rocket run verifies the corrected outward landing-strut geometry with an engine/feet close-up.
- For each of the 11 GUIs, the real NEI item panel is populated with stable existing quarry/loader and space-machine icons. Framebuffer captures with/without button hover compare the sampled item pixels exactly. GUI color remains white; GL errors are checked. A full resource reload then renders the cached textured rocket again.
- Real Earth → Moon → Earth → Mars → Earth survival round trip after preparing fixtures: server fuel use, actual travel phases, retained inventory/XP, terrain landing, finite canister refueling, gravity, helmet removal in a pressurized base and a wall breach. This flight completed before the final minor armor-fit/landing-strut appearance corrections; unchanged common gameplay bytecode is checked against the baseline, and the final models were separately rechecked.
- Exact final production JAR on standalone and original HBM X5771 dedicated servers, with save/restart: crafting, physical assembly, HDD/telescope powered research, fuel manufacture, real air/filter/suit/canister conservation, terrain, pressure/leaks, physical two-door interlocks, launchpad validation, lamp lighting, recovery and flight identity. HBM tests use a genuinely placed storage tank and exposed fluid port, not a fabricated adapter.
- Upgrade: a copy of the earlier disposable 2.0.0 standalone server save loads under 2.0.1. Existing stored launch assembly, researched HDD, recovered fluids, suit output, planetary save folder and lamp light persist. Every source-save file hash is verified unchanged. No human player's game world is used as a fixture.
- Release audit: all legacy artwork/reference bytes retained; changes from the 2.0.0 source baseline confined to the visual whitelist; core gameplay bytecode unchanged; all 109 PNGs match the recorded hashes and packaged resources; final production JAR bytes match each dedicated-server runtime copy; no tests or third-party Minecraft/HBM/NEI classes bundled.

## Practical limits

The full flight and model/GUI checks run in a development client, while the final packaged JAR is verified in the native production dedicated servers. This is not an exhaustive test of every GPU, shader/OptiFine configuration, arbitrary resource pack, multiplayer combination or long-running survival game. The block/item art is deliberately 16px and the rocket remains a low-poly legacy mesh. Existing Earth sky art was retained for a possible separate pass. Old 2.0.0 gameplay/regression evidence remains in `docs/TESTING-2.0.0.md` and `docs/test-results/2.0.0/`; its older counts are not presented as newly rerun tests.

All current artifacts and screenshots are under `docs/test-results/2.0.1/`. Existing installed Minecraft worlds and mod files were untouched. Install by replacing the older IndustrialTechnology JAR with `IndustrialTechnology-1.7.10-2.0.1.jar`, keeping the same configuration and saves.
