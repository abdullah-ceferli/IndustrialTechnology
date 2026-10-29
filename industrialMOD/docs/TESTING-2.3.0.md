# Verification — Industrial Technology 2.3.0

Executed with Java 8, Minecraft 1.7.10 and Forge 10.13.4.1614. Reports are retained in
`docs/test-results/2.3.0/`; repeated checks in different runtime configurations count as separate executions.

| Archived report | Passed checks |
| --- | ---: |
| TEST-dev.industrialtechnology.automation.AutomationReferenceTest.xml | 2 |
| TEST-dev.industrialtechnology.automation.RepeatTimingTest.xml | 7 |
| TEST-dev.industrialtechnology.automation.ScriptCompilerTest.xml | 12 |
| TEST-dev.industrialtechnology.automation.ScriptPacketTest.xml | 5 |
| TEST-dev.industrialtechnology.capacitor.BankLedgerTest.xml | 16 |
| TEST-dev.industrialtechnology.client.CodeEditorModelTest.xml | 6 |
| TEST-dev.industrialtechnology.quarry.AreaCursorTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.BoundaryGeometryTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.MiningRateTest.xml | 3 |
| TEST-dev.industrialtechnology.wireless.DeviceSearchTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.ReactorSettingsTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.WirelessCatalogTest.xml | 5 |
| TEST-dev.industrialtechnology.wireless.WirelessCommandTest.xml | 4 |
| capacitor-standalone-initial.txt | 19 |
| capacitor-standalone-restart.txt | 1 |
| capacitor-pipes-hbm-initial.txt | 21 |
| capacitor-pipes-hbm-restart.txt | 1 |
| original-standalone-initial.txt | 139 |
| original-standalone-restart.txt | 12 |
| original-hbm5771-initial.txt | 176 |
| original-hbm5771-restart.txt | 13 |
| capacitor-client.txt | 12 |
| Total recorded successful executions | 472 |

The 78 unit tests include all 62 retained tests and 16 bank ledger tests: simulation, shared
budgets, per-cell ownership on merge/split, refunds, reentrancy reservations, 5 billion RF storage,
4 billion RF/t aggregate limits, previous-tick statistics through reconstruction and 10,000 randomized
conservation operations. They do not rely on a live Minecraft server.

The exact distribution JAR is copied into **four fresh native Forge server configurations**, each
started twice in a separate JVM for real save/restart verification. The standalone bank suite and
the retained original suite use no HBM, Ender IO or Thermal mods. The combined bank suite uses the
actual installed HBM X5771, CoFHCore 3.1.4-329, Thermal Foundation 1.2.6-118, Thermal Expansion
4.1.5-248, Thermal Dynamics 1.2.1-172, EnderCore 0.2.0.40_beta and Ender IO 2.3.0.430_beta.
The retained original suite is repeated separately with the installed HBM X5771.

Bank runtime checks cover every six-face adjacency, diagonal isolation, two/five-bank rows,
vertical towers, irregular L shapes, 3x3 walls, 3x3x3 cubes, a 200-bank 5-billion-RF structure,
shared distant input/extraction, multi-face budgets, output modes, rate limits, redstone, splits,
charged survival harvesting/replacement and ordinary server restarts. A partial-accept receiver
tries reentrant input/extraction; RF and unused quota are refunded exactly. Genuine provider chunk
unloads remove inaccessible RF and signals, queries do not reload those chunks, and explicit reloads
restore cell balances. No synthetic unload event is substituted for actual unloading.

A 20,000 RF/t generator fixture feeds five connected banks and the **real Quarry MK3**. After warm-up,
it mines exactly 500 blocks in 100 server ticks (100 blocks/second at 20 TPS). Received RF equals mined
block costs plus all remaining buffers. There are no topology rebuilds during 130 active ticks.
The combined pack moves **1,000,000 RF through three real resonant fluxduct blocks** and separately
**1,000,000 RF through three real Ender IO energy conduit blocks** between disconnected banks.

The full retained native suites exercise ore compatibility (including installed HBM ores), Fortune,
Silk Touch, claims/events, protected machine inventories, void filters, all three quarry speed targets,
chunk tickets, wireless endpoints, code timing/EVERY, reactor monitoring and fuel automation, retained
green lamps/levers and persistence. The release audit also compares original bytecode and artwork to
2.2.1: only five existing integration source files are intentionally modified (the original snapshot runnable also has shifted debug line numbers, with identical instructions/signatures); original machine, reactor,
quarry, wireless, automation, lighting and RF API bytecode remains identical.

A real Minecraft development client, using the final sources and actual NEI coremod, passes 12 checks.
Screenshots were inspected: the original Ender IO cyan gauge is lit correctly, connected borders are
stable, all six icons exist, real GUI packets show shared RF and apply settings, stale window IDs are
rejected, and the actual NEI crafting screen shows the survival recipe. Pixel comparisons prove NEI
item colors stay identical on rate/side button hover, and tooltips preserve GUI lighting and caller
color. No OpenGL error is reported. Images are archived beside the reports.

The seven block PNGs are **byte-for-byte copies** of the user's installed Ender IO assets, as explicitly
requested after the initial original-art implementation. The source license, NOTICE and credit are
included. Rendering uses cached block meshes and quantized charge synchronization; no bank TESR,
emitted-light updates, per-tick flood fill or forced chunk ticket is added.

Limits: cable interoperability is verified for these exact installed versions, not every RF mod/version.
No direct HE adapter or HBM HE-to-RF converter was tested. X5808 and a full copy of the user's main
world were not rerun for this release; prior evidence remains in the older version reports. The new
client checks use a development build; exact packaged bytes are tested on native dedicated servers.
Crash-during-save cross-chunk transactional recovery and massive live multiplayer/load benchmarks
are not claimed. The existing Minecraft mod installation and human user's worlds were not changed.

Run `tools/build.ps1`, then `tools/verify-capacitor-production.ps1` with a fresh test name (and optionally
`-WithPipes -WithHBM`). `tools/audit_release.py` verifies payload, provenance, source archive and all
recorded successful reports. Previous lamp evidence: [2.2.1 verification](TESTING-2.2.1.md).
