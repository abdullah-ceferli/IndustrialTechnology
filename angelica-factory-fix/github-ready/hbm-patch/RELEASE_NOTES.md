# Angelica 2.2.36 — unofficial HBM patch 3 (experimental)

An experimental compatibility workaround for HBM factory models briefly disappearing with Angelica's section renderer.

**Original Angelica by mitchej123, GT New Horizons, and the Angelica contributors:** https://github.com/GTNewHorizons/Angelica

This patch uses live HBM render bounds and recovers visible, renderable HBM machines omitted from collected section lists. It removes duplicate/invalid HBM entries while preserving the original dispatcher's distance and render-pass checks.

**Known limits:** the exact original bedrock-ore factory flicker is not yet confirmed fixed in a controlled before/after test. The workaround can cost performance in large factories. It is not a general low-FPS fix.

Replace the existing Angelica JAR; keep only one. Requires Minecraft 1.7.10, Forge 10.13.4.1614, UniMixins 0.1.23+, and GTNHLib 0.11.19+. If installed, CodeChickenCore must be 1.2.0+. Do not combine with OptiFine, FastCraft, or BetterFPS. Back up your instance and test in a copy of an affected world.

The JAR is an overlay of the official 2.2.36 release, not a full Gradle rebuild. Matching source, build scripts, licenses, and credits are included in the source archive and repository. See the README for details.
