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
authored in `tools/create_assets.py`. No Thermal Expansion, Ender IO, BuildCraft,
or HBM artwork or code is copied into this mod.
