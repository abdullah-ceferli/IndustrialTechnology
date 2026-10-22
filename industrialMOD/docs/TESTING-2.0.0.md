# Verification — Industrial Technology 2.0.0

All listed reports finish with `Failed: 0`. Counts are recorded test executions, including repeated compatibility runs, not distinct features. Earlier failed diagnostic runs remain in their disposable directories and are excluded.

| Recorded report | Passed checks |
|---|---:|
| legacy-standalone-initial.txt | 128 |
| legacy-standalone-restart.txt | 11 |
| legacy-hbm-initial.txt | 165 |
| legacy-hbm-restart.txt | 12 |
| space-standalone-initial.txt | 22 |
| space-standalone-restart.txt | 4 |
| space-hbm5771-initial.txt | 25 |
| space-hbm5771-restart.txt | 4 |
| space-hbm5808-initial.txt | 25 |
| space-hbm5808-restart.txt | 4 |
| space-conflict-initial.txt | 2 |
| space-conflict-restart.txt | 2 |
| space-client-standalone.txt | 21 |
| space-client-hbm5771.txt | 21 |
| space-sky.txt | 4 |
| reactor-client.txt | 27 |
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
| Total recorded checks, including duplicate compatibility runs | 543 |

Native production runs use reobfuscated release copies and real Forge 10.13.4.1614 dedicated servers, followed by actual saves/restarts. Legacy headless regression runs explicitly disable the new idle policy so their original no-player quarry/ticket expectations remain comparable. Separate space tests exercise suspension/resume and a remaining player. Space conflict tests deliberately pre-register a different provider at the configured lunar ID.

The space client traverses Earth → Moon → Earth → Mars → Earth in survival mode after fixture preparation. It uses real SimpleImpl packets, animated vehicle phases, server fuel consumption, custom dimensions, vanilla inventory/XP, gravity, pressurized-room helmet removal and a wall breach. Crafting and research tests use server fixtures; no claim is made that an entire material progression was hand-played from an empty survival inventory. The HBM client also loads NEI. Focused sky screenshots compare near-side Earth and its absence on the far side at low render distance.

HBM native compatibility checks run against original X5771 and X5808 jars. The legacy reactor client exercises the existing ZIRNOX/RBMK interfaces. Actual tests and screenshots are archived under `docs/test-results/2.0.0/`. The final release auditor verifies Java 8 bytecode, forbidden bundled classes, original texture/reference preservation, exact native runtime jar hashes and source-archive documentation.

Not exhaustively exercised: two independently connected human clients, every third-party claim/energy/fluid mod, full-size maximum-volume bases under sustained server load, NASA scientific-file preprocessing, and hours-long survival balancing. These are limits of performed testing, not claims of failure. The mod does not provide universal HBM pipe or portable gas-item integration; see the space guide's supported tank route.

No existing user save was edited, deleted or used as a fixture. All new test worlds live under fresh project `run-test-*` folders. Earlier 1.6.3 evidence is preserved in `docs/TESTING-1.6.3.md` and its original result directory.
