# Space visual rework — Industrial Technology 2.0.1

Space-only art pass requested 2026-10-21. The installed game and its saves were not used for editing or testing. This patch continues the existing `industrialtech` mod; no second gameplay mod or registry renames.

## Implemented artwork

109 original PNG assets: 68 existing space textures repainted and 41 new face/model/GUI textures. All 11 machines have separate front, side, top and underside resources. The computer has a beige drive cabinet, CRT, keyboard and green phosphor screen; the mounted telescope has support legs, an optical tube, mechanical mount and lens. The assembly bench has a small gantry. The rocket uses a 16-sided textured hull, capsule windows, tapered nose, dark bell, swept fins and separate landing struts.

Items have individually drawn silhouettes: HDD platter/actuator/connector, guidance computer, electronics board, engine plumbing, nozzle, tank variants, fins, folded legs, capsule, tiled heat shield, shell, rocket, canister, filter and metal ingots. The related suit tiers have independent helmet/chest/leg/boot islands, dedicated pack UVs, progressively deeper packs, helmet valves, reinforced shoulder/knee geometry and advanced sole/brow details. The lower half of each 64 x 64 sheet is reserved for those added model parts. Leggings sit beneath the outer helmet/chest/boot layer to prevent coincident armor surfaces.

Lunar materials use explicit fractured clasts, breccia inclusions and quiet dust grains; Mars uses a separate brown/red palette and sedimentary bedding. Airtight hulls, clear framed glass, two door halves, door edges, pad grates, lamp states and own fluid atlas tiles were also repainted.

The space GUI has a baked pixel panel, aligned slot wells, compact bevel buttons, a status strip and useful RF/fluid/research readouts. Unused gauges are hidden on research/optics equipment. The 320 x 238 container footprint and all slot indices, button actions and packet IDs stay unchanged. Older GUIs were not redesigned.

## Art method and scope

`tools/create_space_art.py` draws explicit pixel coordinates, silhouettes and restricted palettes using Pillow. No AI image generator, random-noise fill, borrowed texture, tracing, smooth filter, PBR map or Blender asset was used. This is low-resolution original artwork, not a claim of a human external artist's commission. [Asset board](art/space-art-board.png) and [SHA256 inventory](art/space-art-manifest.json) make the result reviewable and reproducible.

Machine placement now stores horizontal facing 2..5. Existing metadata 0/1 keeps a south-facing front, with no world conversion. World and inventory face selection are separate so inventory machines display a recognizable panel. The computer, telescope and gantry rotate with the same facing. Door metadata retains its original open/upper-half bits; only its icons change. The hull/pad retain full-cube collision, hardness, resistance and existing registry IDs.

The new model meshes cache geometry in OpenGL display lists and bind textures outside the cache, supporting resource reloads. Rendering uses world/entity lightmaps, deliberate face shading and scoped matrix/attribute state. The initial overly dark preview was corrected before release. Inventory, oxygen, fuel, travel, research, pressure, airlock and HBM integration implementations were preserved.

## Files changed

- `src/main/resources/assets/industrialtech/textures/`: exact inventory below.
- `client/space/SpaceModels.java`, `SpaceMachineRenderer.java`, `RocketRenderer.java`, `SpaceSuitModel.java`, `GuiSpace.java`.
- `space/BlockSpaceMachine.java`, new `BlockSpaceStructure.java`, face registration in `BlockAirlockDoor.java`, structural block constructors in `SpaceRegistry.java`, suit model constructor in `ItemSpaceSuit.java`.
- Version declaration in `build.gradle` and `core/IndustrialTechnology.java`; no registry or NBT schema version changes.
- Authoring script, test-only visual harness, release audit and this documentation/evidence. Full baseline-relative source inventory is recorded with the release audit.

## Verification and remaining limits

See `docs/TESTING.md` and `docs/test-results/2.0.1/` for the final performed checks. Intermediate diagnostic runs are retained separately and are not counted as successful release evidence. No existing player world is modified. Compatibility testing uses a copied disposable 2.0.0 server save.

The art is intentionally 16px and low-poly. It is not a high-resolution simulation model. The existing Earth sky-disc asset and sky effects were left unchanged; they can receive a separate art pass. No known missing texture or broken model is accepted for release. Arbitrary resource packs, shader mods, and every GPU/OptiFine configuration are not exhaustively tested.

## Exact texture inventory

Paths are relative to `src/main/resources/assets/industrialtech/textures/`.

| Texture | Change | Pixels |
|---|---|---|
| `blocks/research_computer.png` | repainted | 16 x 16 |
| `blocks/research_computer_side.png` | new | 16 x 16 |
| `blocks/research_computer_top.png` | new | 16 x 16 |
| `blocks/research_computer_bottom.png` | new | 16 x 16 |
| `blocks/telescope.png` | repainted | 16 x 16 |
| `blocks/telescope_side.png` | new | 16 x 16 |
| `blocks/telescope_top.png` | new | 16 x 16 |
| `blocks/telescope_bottom.png` | new | 16 x 16 |
| `blocks/rocket_assembly.png` | repainted | 16 x 16 |
| `blocks/rocket_assembly_side.png` | new | 16 x 16 |
| `blocks/rocket_assembly_top.png` | new | 16 x 16 |
| `blocks/rocket_assembly_bottom.png` | new | 16 x 16 |
| `blocks/fuel_refinery.png` | repainted | 16 x 16 |
| `blocks/fuel_refinery_side.png` | new | 16 x 16 |
| `blocks/fuel_refinery_top.png` | new | 16 x 16 |
| `blocks/fuel_refinery_bottom.png` | new | 16 x 16 |
| `blocks/oxygen_plant.png` | repainted | 16 x 16 |
| `blocks/oxygen_plant_side.png` | new | 16 x 16 |
| `blocks/oxygen_plant_top.png` | new | 16 x 16 |
| `blocks/oxygen_plant_bottom.png` | new | 16 x 16 |
| `blocks/air_compressor.png` | repainted | 16 x 16 |
| `blocks/air_compressor_side.png` | new | 16 x 16 |
| `blocks/air_compressor_top.png` | new | 16 x 16 |
| `blocks/air_compressor_bottom.png` | new | 16 x 16 |
| `blocks/suit_refill_station.png` | repainted | 16 x 16 |
| `blocks/suit_refill_station_side.png` | new | 16 x 16 |
| `blocks/suit_refill_station_top.png` | new | 16 x 16 |
| `blocks/suit_refill_station_bottom.png` | new | 16 x 16 |
| `blocks/atmosphere_pressurizer.png` | repainted | 16 x 16 |
| `blocks/atmosphere_pressurizer_side.png` | new | 16 x 16 |
| `blocks/atmosphere_pressurizer_top.png` | new | 16 x 16 |
| `blocks/atmosphere_pressurizer_bottom.png` | new | 16 x 16 |
| `blocks/launch_controller.png` | repainted | 16 x 16 |
| `blocks/launch_controller_side.png` | new | 16 x 16 |
| `blocks/launch_controller_top.png` | new | 16 x 16 |
| `blocks/launch_controller_bottom.png` | new | 16 x 16 |
| `blocks/fuel_port.png` | repainted | 16 x 16 |
| `blocks/fuel_port_side.png` | new | 16 x 16 |
| `blocks/fuel_port_top.png` | new | 16 x 16 |
| `blocks/fuel_port_bottom.png` | new | 16 x 16 |
| `blocks/airlock_controller.png` | repainted | 16 x 16 |
| `blocks/airlock_controller_side.png` | new | 16 x 16 |
| `blocks/airlock_controller_top.png` | new | 16 x 16 |
| `blocks/airlock_controller_bottom.png` | new | 16 x 16 |
| `blocks/airtight_hull.png` | repainted | 16 x 16 |
| `blocks/airtight_hull_top.png` | new | 16 x 16 |
| `blocks/rocket_hull.png` | repainted | 16 x 16 |
| `blocks/rocket_hull_top.png` | new | 16 x 16 |
| `blocks/rocket_launchpad.png` | repainted | 16 x 16 |
| `blocks/rocket_launchpad_side.png` | new | 16 x 16 |
| `blocks/airtight_glass.png` | repainted | 16 x 16 |
| `blocks/airlock_door.png` | repainted | 16 x 16 |
| `blocks/airlock_door_top.png` | new | 16 x 16 |
| `blocks/airlock_door_edge.png` | new | 16 x 16 |
| `blocks/green_lamp_off.png` | repainted | 16 x 16 |
| `blocks/green_lamp_on.png` | repainted | 16 x 16 |
| `blocks/moon_rock.png` | repainted | 16 x 16 |
| `blocks/lunar_basalt.png` | repainted | 16 x 16 |
| `blocks/lunar_breccia.png` | repainted | 16 x 16 |
| `blocks/mars_rock.png` | repainted | 16 x 16 |
| `blocks/lunar_regolith.png` | repainted | 16 x 16 |
| `blocks/lunar_dust.png` | repainted | 16 x 16 |
| `blocks/mars_regolith.png` | repainted | 16 x 16 |
| `blocks/mars_layered_rock.png` | repainted | 16 x 16 |
| `blocks/subsurface_ice.png` | repainted | 16 x 16 |
| `blocks/fluids/crude_oil.png` | repainted | 16 x 16 |
| `blocks/fluids/kerosene.png` | repainted | 16 x 16 |
| `blocks/fluids/oxygen.png` | repainted | 16 x 16 |
| `blocks/fluids/liquid_oxygen.png` | repainted | 16 x 16 |
| `blocks/fluids/breathable_air.png` | repainted | 16 x 16 |
| `items/rocket_engine.png` | repainted | 16 x 16 |
| `items/rocket_fuel_tank.png` | repainted | 16 x 16 |
| `items/oxidizer_tank.png` | repainted | 16 x 16 |
| `items/guidance_computer.png` | repainted | 16 x 16 |
| `items/navigation_electronics.png` | repainted | 16 x 16 |
| `items/rocket_hull.png` | repainted | 16 x 16 |
| `items/rocket_fins.png` | repainted | 16 x 16 |
| `items/rocket_nozzle.png` | repainted | 16 x 16 |
| `items/crew_capsule.png` | repainted | 16 x 16 |
| `items/heat_shield.png` | repainted | 16 x 16 |
| `items/landing_legs.png` | repainted | 16 x 16 |
| `items/research_hdd.png` | repainted | 16 x 16 |
| `items/assembled_rocket.png` | repainted | 16 x 16 |
| `items/space_canister.png` | repainted | 16 x 16 |
| `items/space_steel.png` | repainted | 16 x 16 |
| `items/space_titanium.png` | repainted | 16 x 16 |
| `items/thermal_alloy.png` | repainted | 16 x 16 |
| `items/air_filter.png` | repainted | 16 x 16 |
| `armor/suit_1_1.png` | repainted | 64 x 64 |
| `armor/suit_1_2.png` | repainted | 64 x 64 |
| `items/spacesuit_1_0.png` | repainted | 16 x 16 |
| `items/spacesuit_1_1.png` | repainted | 16 x 16 |
| `items/spacesuit_1_2.png` | repainted | 16 x 16 |
| `items/spacesuit_1_3.png` | repainted | 16 x 16 |
| `armor/suit_2_1.png` | repainted | 64 x 64 |
| `armor/suit_2_2.png` | repainted | 64 x 64 |
| `items/spacesuit_2_0.png` | repainted | 16 x 16 |
| `items/spacesuit_2_1.png` | repainted | 16 x 16 |
| `items/spacesuit_2_2.png` | repainted | 16 x 16 |
| `items/spacesuit_2_3.png` | repainted | 16 x 16 |
| `armor/suit_3_1.png` | repainted | 64 x 64 |
| `armor/suit_3_2.png` | repainted | 64 x 64 |
| `items/spacesuit_3_0.png` | repainted | 16 x 16 |
| `items/spacesuit_3_1.png` | repainted | 16 x 16 |
| `items/spacesuit_3_2.png` | repainted | 16 x 16 |
| `items/spacesuit_3_3.png` | repainted | 16 x 16 |
| `models/expedition_rocket.png` | new | 128 x 128 |
| `models/space_equipment.png` | new | 128 x 64 |
| `gui/space_console.png` | new | 512 x 256 |
