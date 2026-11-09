# Third-party code

The unmodified Redstone Flux energy API and `CoFHAPIProps` were obtained from
[CoFHLib 1.7.10](https://github.com/CoFH/CoFHLib-1.7.10-Legacy/tree/6b24c7dc3b6bbcb1c22fe1051387d816f3cb1f3b/src/main/java/cofh/api),
commit `6b24c7dc3b6bbcb1c22fe1051387d816f3cb1f3b`, API version `1.7.10R1.3.1`.
Copyright Team CoFH; upstream repository license: LGPLv3. The upstream license
is preserved in `vendor/cofh/LICENSE.txt` and in the binary JAR under
`META-INF/licenses/CoFH-RF-LGPL-3.0.txt`. Complete API sources are in the repository
and the accompanying sources JAR. Replace/rebuild them normally with Gradle.

The Gradle wrapper comes from the
[maintainer's ForgeGradle example](https://github.com/anatawa12/ForgeGradle-example)
and downloads Gradle 7.4.2 (Apache License 2.0). The pinned
[ForgeGradle 1.2 maintenance fork](https://github.com/anatawa12/ForgeGradle-1.2)
is `com.anatawa12.forge:ForgeGradle:1.2-1.1.1` (LGPL-2.1).
Forge 10.13.4.1614 and Minecraft are development dependencies, never included
inside the mod JAR. JUnit 4.13.2 is test-only.

All machine faces, icons, and GUI panels are original code-drawn pixel artwork
authored in `tools/create_assets.py` and `tools/create_wireless_assets.py`. The pre-existing original machine artwork is authored here. Later explicitly requested Ender IO bank and solar asset reuse is described below; no HBM or railway-mod artwork/code is copied.

## Capacitor artwork (2.3.0 personal build)

At the user's explicit request, the capacitor uses seven unchanged Ender IO PNGs from their installed
`EnderIO-1.7.10-2.3.0.430_beta.jar`, rather than the initial original artwork.
Credit: CrazyPants and Ender IO contributors. The source JAR's Apache-2.0 LICENSE is included in
`META-INF/licenses/EnderIO-Apache-2.0.txt`; texture provenance is also recorded in the JAR.
No Ender IO Java code or RF implementation is bundled.

## Modern photovoltaic artwork (2.4.0)

Team Ender IO / contributors, EnderIO 26.3 commit 9ec8183274d61b57ae0562c5d9b4bd486fef8f0c, Unlicense. Six photovoltaic surface/side PNGs are reused unchanged; original connected tier frames and legacy mesh adapt the modern 16x2x16 model. License is packaged as META-INF/licenses/EnderIO-Modern-Unlicense.txt. Full mapping, references and original railway/audio attribution: ASSET_CREDITS.md.

## Complete OpenComputers (2.9.0)

MightyPirates / Sangar and OpenComputers contributors: original MC1.7.10 OpenComputers 1.8.10, commit 667626d. Entire original universal runtime and original art/programs are embedded unchanged except combined archive metadata. Upstream source: https://github.com/MightyPirates/OpenComputers . Code is MIT; original asset and bundled-dependency notices remain in their original archive paths, with additional copies under vendor/opencomputers/notices. This includes LICENSE, LICENSE-mods, LICENSE-unifont, LICENSE-jnlua, LICENSE-wcwidth, LICENSE-luaj and LICENSE-typesafe. The source JAR contains original upstream Scala/Java sources and resources under opencomputers/. Provenance/hashes: vendor/opencomputers/upstream.json. No AI textures are introduced.

## Independent accessories and catalysts (2.10.0 personal build)

Original sprites for Baublelicious Flight Ring, Nightvision Amulet, base ring/amulet and three Magic Cores: DenBukki / UnwrittenFun, installed baublelicious-1.7.10-1.2.2-final.jar. Original ProjectE Destruction Catalyst, Hyperkinetic Lens, Catalytic Lens, lens/loot entity sprites and four relevant sounds: ProjectE/EE2 contributors, installed ProjectE-1.7.10-PE1.10.1.jar. Source-to-target SHA256 mapping and original archive hashes: development-assets/accessories/original-assets.json. All copied art/audio bytes are unchanged. No AI assets are added.

NovaExplosion is adapted from the installed original ProjectE PE1.10.1 implementation (ray/resistance collection with no entity damage), with names/helper calls changed for independence and added loaded-block/break protection. Original ProjectE MC17 README/credits remain packaged under META-INF/notices/ProjectE-MC17-README.md. Original code/asset ownership remains with the respective authors; this section does not relicense it. Primary sources: https://github.com/sinkillerj/ProjectE , https://www.curseforge.com/minecraft/mc-mods/baublelicious .

Baubles by Azanor is an external required dependency supplying its original API, UI, networking and saved equipment. Its verified original JAR under vendor/baubles is used for compilation/development only and is not embedded. Source: https://github.com/Azanor/Baubles . ProjectE and Baublelicious are not runtime dependencies of the new items, and their installed JARs are left untouched.
