# Verification — Industrial Technology 2.2.0

Java 8 / Forge 10.13.4.1614. Reports below are from performed runs of this removal release. Diagnostic failures and earlier releases are excluded. Compatibility runs repeat some assertions and count as separate executions.

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
| production-standalone-initial.txt | 139 |
| production-standalone-restart.txt | 12 |
| production-hbm5771-initial.txt | 176 |
| production-hbm5771-restart.txt | 13 |
| upgrade-from-2.1.0.txt | 4 |
| lamp-client.txt | 3 |
| Total recorded successful executions | 409 |

## Validated behavior

- The exact reobfuscated distribution JAR runs on native dedicated Forge servers without HBM and with the user's actual HBM X5771, followed by a real JVM save/restart in each case. The original quarry mining/rate/ore/protection, chunk-ticket, wireless, automation, saved settings and real reactor checks pass.
- All 66 former space block/item registry names and the five space fluids are absent, Moon/Mars are unregistered, and the retained lamp crafts four blocks with its original recipe.
- Survival item placement supports vanilla levers on all six green lamp faces. Each switches actual emitted and cached block light, remains attached in both states, and a powered lamp/lever pair survives restart.
- A copy of a saved 2.1.0 dedicated test world loads after removal: old space blocks become air, their tile entities disappear, the old powered green lamp and ordinary redstone source survive, and old planetary region folders remain on disk.
- Real Minecraft client with HBM X5771 + NEI: a copied 2.1.0 single-player save with its saved player dimension changed to the removed Moon joins in the Overworld, preserves 23 diamonds and XP level 37, places/toggles all six levers in survival and renders the lamps without an OpenGL error. The screenshot is archived and inspected. The early server player recovery and stale Forge handshake override correction are both exercised.
- Native player-NBT checks recover both removed default planet IDs while preserving inventory/XP, and leave ordinary Overworld position data alone.
- Original source-save hashes remain unchanged. No installed user worlds or installed mod JARs were edited. All worlds used for testing are disposable project fixtures or copies.
- The release audit compares exact runtime JAR identities, Java 8 bytecode, absence of space implementation and third-party/test classes, preservation of original pre-space subsystem bytecode (except core registration/events), original art/reference bytes, retained lamp textures and current source documentation.

## Limits

Client rendering/login uses a development client; the exact packaged JAR is validated on native dedicated servers. These checks do not cover every mod fork, GPU, resource pack, claim plugin or long survival session. Custom legacy planet IDs are read from the existing old config; client/server legacy dimension settings should match. Dimensions currently registered by another mod are left alone.

Removed space contents disappear when an old world is opened and saved. Back up the complete save before replacing the mod. World directories, planetary region files and old configs are not deleted. See [changes and save compatibility](CHANGES-2.2.0.md). Earlier evidence is retained in [the 2.1.0 verification](TESTING-2.1.0.md).
