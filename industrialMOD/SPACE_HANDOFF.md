# Industrial Technology handoff — 2.1.0

Project: `D:\IndustrialTechnology\industrialMOD`. Minecraft Forge 1.7.10 / Java 8. Existing mod ID: `industrialtech`. New release: `build/libs/IndustrialTechnology-1.7.10-2.1.0.jar`.

## Latest user request

Build a much larger single-structure launchpad and a rocket using real 1:1 principal dimensions. User chose **Space Shuttle + Launch Complex 39**. Add realistically motivated lunar/Martian resources and native HBM kerosene/LOX pipe input. User additionally requires the products, especially titanium, to work in HBM crafting.

## Implemented

- Deployable LC-39 kit with 119 x 99 m hardstand, 49 x 41 x 8 m MLP, pedestal supports, flame openings/deflectors, 75 m service tower/mast to 106 m, parked RSS framework, ladder and animated retracting access arms.
- 56.1 m Shuttle stack: orange ET, twin boosters, 37.2 m orbiter and 23.8 m wingspan. A separate assembly type preserves old saved rockets and their smaller pad.
- Checked, batched construction/dismantling. No terrain clearing. Blocked/claimed/overlapping attempts recover one kit; inventory, settings and fluids persist. Save/restart resumes work. Ordinary parts have no individual tile entities or item drops.
- Real optional HBM MK2 pipe receiver on the LC-39 root and existing fuel port; typed KEROSENE, cryogenic OXYGEN -> LOX and AIR; finite shared rate and loaded/protected network checks.
- Moon iron rock, ilmenite and feldspar; Mars hematite, titanium rock and silica; water extraction recipe for the existing regional subsurface ice. New chunks only. Outputs use actual HBM titanium/aluminium ingots when HBM is installed, vanilla iron and quartz otherwise where appropriate. Standalone fallback remains playable.

## Read next

- `SPACE_EXPLORATION_GUIDE.md`: player-facing recipes, placement, launch, pipes and metal processing.
- `docs/SHUTTLE_LC39_RESOURCES.md`: NASA references, scale, geology and engineering limits.
- `docs/TESTING.md`: performed validation and archived evidence for this release.
- `docs/art/shuttle-resources-board.png`: new pixel art; existing space and older quarry/loader art is preserved.

## Constraints and history

The real Shuttle used hydrogen/oxygen and flew in Earth orbit. Requested kerosene/LOX and Moon/Mars vertical stack travel remain game abstractions; details are not a surveyed NASA replica. Old 2.0.1 evidence is archived separately and is not counted as newly executed checks. Earlier quarry, chunk-loading, wireless, automation and HBM reactor systems remain part of this same mod.

Never delete the user's Minecraft worlds or use them as test fixtures. Tests use new disposable project run directories. The installed game and its existing saves are not modified by this task.

The configured cwd `D:\industrialMOD` is stale; the actual checkout is the path above. Building needs Java 8, cached ForgeGradle tooling, and the verified compile-only HBM development API at `.toolchain/hbm-api/HBM-X5771-dev.jar`; that dependency is not packaged in the release.
