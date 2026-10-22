# Industrial Technology handoff — 2.0.1

Minecraft Forge 1.7.10 / Java 8, same `industrialtech` mod. Current deliverable: `build/libs/IndustrialTechnology-1.7.10-2.0.1.jar`. Replace the older IndustrialTechnology JAR when installing, retaining the existing config and saves.

The latest request was a space-only visual rework. Implemented 109 original pixel textures, distinct machine faces and facing, textured rocket/CRT/telescope/workbench meshes, related suit tiers with dedicated UVs, new space GUI artwork and individually readable component icons. Quarry, chunk-loader, wireless, reactor and old GUI art were retained. Gameplay registry names, NBT formats, fuel/oxygen/pressure behavior and HBM adapters are unchanged.

Read `docs/SPACE_VISUAL_REWORK.md` for the exact changed asset inventory, authoring method and remaining art scope. `docs/art/space-art-board.png` is the pixel-art board. `docs/TESTING.md` and `docs/test-results/2.0.1/` contain the actual release evidence. The authoring source is `tools/create_space_art.py`; custom models remain normal 1.7.10 fixed-function Java meshes. Blender was not needed.

The previous implementation and its limits remain documented in `docs/SPACE_HANDOFF-2.0.0.md`, `SPACE_EXPLORATION_GUIDE.md` and `SPACE_TECHNICAL_NOTES.md`. There is still no creative Moon teleport command; travel is through the existing rocket system.

Existing user worlds must never be deleted or used as fixtures. All current tests use fresh `run-test-*` directories or a copy of an earlier disposable test save. The actual installed game is not automatically updated by this task.
