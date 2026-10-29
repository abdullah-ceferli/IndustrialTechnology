# Verification — Industrial Technology 2.2.1

The plain green lamp patch is checked with Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614. Repeated assertions count as separate executions.

| Archived report | Passed checks |
|---|---:|
| TEST-dev.industrialtechnology.automation.AutomationReferenceTest.xml | 2 |
| TEST-dev.industrialtechnology.automation.RepeatTimingTest.xml | 7 |
| TEST-dev.industrialtechnology.automation.ScriptCompilerTest.xml | 12 |
| TEST-dev.industrialtechnology.automation.ScriptPacketTest.xml | 5 |
| TEST-dev.industrialtechnology.client.CodeEditorModelTest.xml | 6 |
| TEST-dev.industrialtechnology.quarry.AreaCursorTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.BoundaryGeometryTest.xml | 5 |
| TEST-dev.industrialtechnology.quarry.MiningRateTest.xml | 3 |
| TEST-dev.industrialtechnology.wireless.DeviceSearchTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.ReactorSettingsTest.xml | 4 |
| TEST-dev.industrialtechnology.wireless.WirelessCatalogTest.xml | 5 |
| TEST-dev.industrialtechnology.wireless.WirelessCommandTest.xml | 4 |
| production-initial.txt | 139 |
| production-restart.txt | 12 |
| lamp-client.txt | 3 |
| Total recorded successful executions | 216 |

- A real development client with HBM X5771 + NEI places and toggles levers on all six lamp faces. The nighttime screenshot was inspected: uniformly green faces with no old casing, darker green when off, bright green when on. No OpenGL error was reported.
- The exact reobfuscated distribution JAR passes the existing native standalone integration suite, including all six survival lever placements and real emitted/cached light values, followed by a genuine JVM restart confirming lamp/lever persistence.
- The release audit requires the distribution to differ from the validated 2.2.0 baseline in exactly the two lamp textures and three version metadata entries. Lamp gameplay, attachments, all other machine behavior, registry identities, migration, artwork and recipes remain byte-for-byte unchanged.
- All 62 existing unit checks pass. The HBM/native old-world upgrade compatibility evidence remains in [the 2.2.0 verification](TESTING-2.2.0.md); those suites were not repeated for this visual-only patch. The copied client save also exercises player recovery again and leaves its original source fixture hashes unchanged.
- No installed Minecraft world or installed mod was edited. All test worlds are disposable project fixtures or copies. Client rendering uses a development build; exact packaged bytes are validated on the native server.

See [the lamp patch notes](CHANGES-2.2.1.md). Normal Minecraft lighting remains unchanged: the lamp face glows green and its light level is 15 when powered.
