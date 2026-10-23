# Verification and known limits

## Automated checks

The Java 8 overlay build and `test.ps1` exercise:

- Dynamic HBM bounds without querying mutable machine bounds during classification.
- Preserved static, infinite, and configured dynamic classification for other mods.
- Recovery of missing HBM models, identity deduplication, invalid entries, and preservation of non-ticking models already collected.
- Off-screen rejection, rejection of proxies without special renderers, and unavailable bounds.
- One inserted fallback call with valid bytecode stack flow.
- Exactly two changed original JAR entries, one added helper, one added provenance note, and byte-identical contents for every other original entry, including existing license notices.

The test fixtures are not Minecraft and are never added to the production JAR. A fixture pass does not prove that an in-game flicker is fixed.

## Previous local runtime observations

The build-3 JAR loaded with Minecraft 1.7.10, Forge 10.13.4.1614, HBM `1.0.27_X5771`, and Java 8u51. The runtime printed the fallback activation message. No linkage/verification errors or renderer exceptions were observed in the final session. Two renderer reloads completed and the visible room models remained present afterward.

A stationary 30-second room capture averaged **94.9 FPS** at a **927 × 562 window size including its frame**. Another separate itemduct optimization mod was active. The original-release comparison used a different view and resolution, so this figure **does not establish an FPS gain from this patch**.

## Still unconfirmed

The specific original bedrock-ore factory route and its momentary disappearance were not reproduced in a controlled before/after test. Stable models in the tested room do not establish a complete fix. The patch may increase rendering cost in large factories. No guarantee is made for other HBM versions or Angelica versions.

The original local logs, world coordinates, installation paths, saved settings, and backups are intentionally absent from this public source package.
