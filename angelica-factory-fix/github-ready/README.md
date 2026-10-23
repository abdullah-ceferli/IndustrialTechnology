# Angelica HBM Render Fix

**An unofficial, experimental HBM factory visibility patch for Angelica 2.2.36 on Minecraft 1.7.10.**

**Angelica is the work of [mitchej123](https://github.com/mitchej123), the [GT New Horizons team](https://github.com/GTNewHorizons/Angelica), and its contributors.** This repository adds a small compatibility workaround; it does not claim authorship of Angelica or endorsement by its developers. Full upstream credits are preserved in [README.upstream.md](README.upstream.md#credits) and summarized in [CREDITS.md](CREDITS.md).

## What this patch tries to help

Some HBM factory models can momentarily disappear while Angelica rebuilds or collects rendered sections. This patch evaluates HBM render bounds live and recovers visible, renderable HBM machines that are missing from the collected render list.

The original bedrock-ore factory flicker has **not yet been confirmed fixed in a controlled before/after test**. This is a workaround to test, not a general solution for low FPS, chunk-update spikes, driver issues, or every rendering bug. Recovering more machines can increase rendering cost in large factories. See [the checks and their limits](hbm-patch/VERIFICATION.md).

## Installation

1. Close Minecraft and back up your instance.
2. Use Minecraft **1.7.10** with Forge **10.13.4.1614**. This patch targets Angelica **2.2.36** specifically.
3. Install Angelica's dependencies: [UniMixins](https://github.com/LegacyModdingMC/UniMixins/releases) **0.1.23 or newer** and [GTNHLib](https://github.com/GTNewHorizons/GTNHLib/releases) **0.11.19 or newer**. If CodeChickenCore is installed, use **1.2.0 or newer**. Check [the upstream compatibility list](README.upstream.md) for other mods in your pack.
4. Replace your existing Angelica JAR with `angelica-2.2.36-personal-hbm3.jar`. Keep **one** Angelica JAR in `mods/`. Do not combine it with OptiFine, FastCraft, or BetterFPS.
5. Test in a copy of an affected world. Compare the same location, direction, resolution, and settings before and after installation.

The output is a replacement Angelica JAR, not an additional mod. It retains upstream's `2.2.36` mod metadata; the filename and `META-INF/personal-hbm-fix.txt` identify the customized build. To revert, close Minecraft and replace it with the official Angelica 2.2.36 JAR.

## Source and building

This repository contains the upstream source and build files plus the HBM changes, based on commit [`e84c8c8d7accd6df0c93340c8409d997a43b9aaa`](https://github.com/GTNewHorizons/Angelica/commit/e84c8c8d7accd6df0c93340c8409d997a43b9aaa).

The tested build method compiles two small classes with Java 8 and inserts one source-equivalent call into the official release using ASM. It preserves every other original JAR entry. It is **not a full Gradle rebuild**. The scripts accept your own JDK and Forge development JAR and download checksum-pinned dependencies instead of depending on the original author's PC paths.

See [BUILDING.md](BUILDING.md) for the tested overlay build and the separate upstream Gradle build. Changes are documented in [hbm-patch/CHANGES.md](hbm-patch/CHANGES.md).

## Reporting results

Use this repository's issue tracker for this unofficial patch. Include the HBM and Angelica versions, the affected machine, steps to reproduce, and a comparison from the same view. Test results from the exact bedrock-ore factory case are especially useful. The patch does not modify world blocks, inventories, simulation, or packet handling.

## License and attribution

Angelica's original [LICENSE](LICENSE) and file-level notices are retained. The HBM additions and build helpers are provided under LGPL v3; copies of LGPL v3 and GPL v3 are included in [licenses/](licenses/). Bundled and borrowed upstream components retain their own licenses; see [CREDITS.md](CREDITS.md), [README.upstream.md](README.upstream.md#credits), and source-file notices.

When sharing a customized binary, also make its matching source and build instructions available. [PUBLISHING.md](PUBLISHING.md) describes the prepared source and release assets.
