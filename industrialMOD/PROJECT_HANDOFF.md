# Industrial Technology handoff — 2.2.1

Actual checkout: `D:\IndustrialTechnology\industrialMOD` (the configured `D:\industrialMOD` path is stale). Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8. Mod ID remains `industrialtech`.

The user requested removal of the entire space expansion, then explicitly asked to **keep the green block and allow a lever on it**. The retained block is the Green Redstone Lamp. Its old registry name and four-block crafting recipe remain. Version 2.2.1 replaces its decorative textures with plain solid green faces: darker green off, bright green on. Regenerate these two flat tiles with `tools/create_lamp_assets.py`. Its six faces now support vanilla levers and torches even though its material is glass. Redstone controls actual light 0/15.

Space Java/client code, providers, dimensions, machines, suits, rockets, launchpads, minerals, recipes, fluids, artwork and live instructions are removed. The exact pre-space edits in eight original classes were restored from the verified 1.6.3 baseline; all other original quarry/wireless/reactor code remains. Space-only chunk-loading suspension was removed. The compile-only HBM development dependency is no longer needed; optional reactor integration remains.

`core/ContentMigration.java` accepts only the exact retired registry names, allowing a previous 2.x Overworld to load without the removed-content prompt. Existing green lamps survive. `core/RetiredDimensionRecovery.java` corrects old Moon/Mars player NBT before the login packet, preserving inventory/XP. It reads existing legacy configured IDs without creating or saving a space config; registered dimensions are left alone. Removed space contents disappear when the user opens and saves a world; old world/dimension/config files are not deleted. See `docs/CHANGES-2.2.0.md`.

Previous 2.1.0 source/docs and JARs were backed up before deletion in `.toolchain/pre-space-removal-2.1.0/`. Earlier test worlds and versioned evidence remain as history. Never delete or use the human user's actual Minecraft worlds as fixtures. No automatic installation into the user's game is part of this update.

Build: `tools/build.ps1`; distribution: `build/libs/IndustrialTechnology-1.7.10-2.2.1.jar`. Current validation is in `docs/TESTING.md` and `docs/test-results/2.2.1/`. The original automation reference is `AUTOMATION_CODES.md`; reactor instructions remain in `docs/REACTORS.md`.
