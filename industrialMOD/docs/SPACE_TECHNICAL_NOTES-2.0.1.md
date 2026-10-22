# Space expansion — technical notes

Version 2.0.1 includes the space-only visual rework documented in `docs/SPACE_VISUAL_REWORK.md`. Core implementation verification below refers to the 2.0.0 baseline; current release checks are separately recorded in `docs/TESTING.md`.

## Architecture and persistence

New common code is under `dev.industrialtechnology.space`; client renderers/GUI/HUD are isolated under `dev.industrialtechnology.client.space`. No second gameplay mod is registered. The separate `tools/space-tests` mod is a development harness and is excluded from the gameplay jar.

`TileSpaceMachine` owns RF, 12 inventory slots, three separate Forge tanks, ownership and versioned NBT. HDD progress is item NBT. A finished rocket has a unique assembled vehicle identity. Flight control retains real inventories, researched HDD, fluids, owner, current phase/ticks, home coordinates and landing coordinates. `FlightLedger` is world saved data and prevents two entities claiming one active vehicle. Recovered payload fluids/HDD are restored transactionally once.

Launch commands use a current-window container, live distance/world/entity checks, owner validation, a four-tick command cooldown and Forge interaction hooks. Packet mutations run through the existing bounded `ServerWork` queue. Dimension transfers are deferred to the following server tick, outside entity-list iteration. Snapshot handlers delegate through the sided proxy, including HUD updates. Client-only suit model methods are explicitly sided; dedicated-server boot is part of validation.

The default reserved dimension and biome IDs are 180/181. Registration checks for collisions before adding providers. Separate save folders are `IndustrialTechMoon` and `IndustrialTechMars`. Existing terrain and registries are not replaced. Planet generation is seed/coordinate deterministic and column-local; it neither populates neighbour chunks nor searches unbounded terrain. Landing search has a configurable finite candidate limit and generates only a small region around the intended site.

Room flood fill uses six neighbours, a packed-coordinate set and an incremental queue. Boundaries, unloaded chunks, maximum volume and radius produce an unsealed result. Room gas is finite and retained across validation; periodic rescans do not themselves depressurize a valid room. Structural invalidation stops maintenance until a seal is re-established. Global scan work is capped. Pressure is a gameplay percentage computed from gas and volume, not a real gas-law/temperature solver. Room upkeep and occupant breathing continue when RF is lost or the machine is paused. Occupied closed airlocks also lose finite pressure while unpowered; disabling a controller cannot create an infinite-air shelter.

The airlock is deliberately a validated fixed chamber, not arbitrary door pairing. Real two-block doors change collision and seal states. Commands enforce mutual exclusion and pressure match; gas recovery is lossy. Room/cell registries are removed on invalidation/world unload. An occupied doorway refuses closure.

`DimensionActivity` stores only this mod's machine UUIDs and coordinates. It releases only ticket lists already owned by `ChunkTickets`, skips quarry accrual/scheduling in suspended worlds, and resumes valid indexed machines with a two-chunk/tick budget. Registry/read limits are finite (100,000 indexed entries). Stale/replaced UUIDs are removed. Travel exemptions are bounded. Vanilla and other mods determine their own spawn chunks, tickets and dimension lifetime.

## NASA sources and scientific scope

- [NASA Moon facts](https://science.nasa.gov/moon/facts/) informed the airless environment, approximate gravity, thermal endpoints, maria/highlands, impact morphology and synchronous-rotation sky assumptions.
- [NASA CGI Moon Kit / LOLA displacement maps](https://svs.gsfc.nasa.gov/4720/) provides a primary lunar relief reference. NASA credits the Scientific Visualization Studio, Ernie Wright and Noah Petro. The source explains its LOLA-derived displacement formats and units.
- [NASA high-resolution topographic map of the Moon](https://science.nasa.gov/resource/high-resolution-topographic-map-of-the-moon/) supplied an additional morphology reference.
- [NASA Mars facts](https://science.nasa.gov/mars/facts/) informed red iron-oxide terrain, thin non-breathable atmosphere, dust, ice and day-length assumptions.
- [NASA global Mars topography](https://science.nasa.gov/photojournal/maps-of-mars-global-topography/) and [Mars topography](https://science.nasa.gov/photojournal/mars-topography/) informed the distinction between plains, volcanic uplift, canyons and regional relief.

Default terrain is original procedural synthesis, not a NASA heightmap reproduction. Lunar bowls/rims are bounded cell-based impact features at two scales; broad fields distinguish basaltic lowlands and rougher highlands. Martian fields add layered relief, two meandering eroded-channel bands, dunes, a volcanic uplift/caldera province and latitude-dependent ice. Landmark dimensions and distances are compressed for Minecraft. Resources, fuel volumes, RF costs and processing ratios are gameplay values rather than a mass-accurate propulsion/atmosphere simulation.

Earth is an original pixel illustration. It is fixed to an approximate near-side lunar coordinate frame, rather than rotating around every observer. X repeats longitude every 65,536 blocks, near-side X≈0, far-side X≈32,768; Z is a bounded latitude approximation. This flat periodic projection is not a spherical lunar surface model. The HUD's °C reading is an illustrative day/night interpolation, not a heat-transfer calculation. Vanilla 1.7.10 skips its sky pass below four render chunks; a planet-only masked late pass preserves Earth/stars at low view distance without raising the user's settings or painting over terrain.

## Optional real height references

Set `optionalHeightReferences=true` and supply `config/industrialtech-space/moon-height.png` and/or `mars-height.png`. Each must be a preprocessed grayscale **PNG with each axis between 2 and 512 pixels**. Header dimensions are checked before decoding; data is read once and stored in bounded integer arrays. No per-chunk disk/network access occurs.

Prepare these files from an appropriately credited NASA elevation source, with darker pixels representing lower elevations. Raw signed LOLA TIFFs and MOLA scientific grids are not read directly; their units, projection and no-data values must be handled during preprocessing. The import maps X periodically across the image and clamps Z to its rows, adding bounded relief (±16 lunar / ±24 Martian blocks) beneath the procedural landforms. Align the projection yourself; this is a relief reference option, not geodetic coordinate registration. No external heightmaps are downloaded or bundled automatically. Keep reference files unchanged for a save: changing generation inputs affects only new chunks and can create seams.

## HBM boundary

Native adapter inspection targets real `com.hbm.inventory.fluid.Fluids`, `FluidTank`, and `TileEntityMachineFluidTank` signatures. Native tank provider modes are honoured through `getSendingTanks()`. Exact AIR/OIL/KEROSENE/OXYGEN objects are matched; no gas-name substring guessing is used. Steel/titanium recipes resolve actual `com.hbm.items.ModItems` fields. Optional classes are accessed reflectively; none are packaged.

Supported HBM storage-tank extraction is intentionally narrower than universal HBM pipe support. It does not implement HBM MK2 network interfaces, directly interpret portable HBM gas-item NBT, or convert arbitrary HE into RF. A native sending storage tank beside the corresponding station supplies the bridge. Its inspected fluid-capable proxies resolve through a loaded-only, 32-step metadata-arrow walk with an eight-block radius; interaction protection is checked at both port and controller. Forge-fluid and own-compressor/canister routes work independently of HBM.

## Practical limits

- Fixed 1×3×2 airlock chamber; doors oriented on Z. Other dimensions/layouts report incomplete structure.
- One authoritative pressurizer per overlapping sealed room; configured radius/volume limits apply.
- Normal Forge block events plus a configurable periodic rescan catch geometry changes. Third-party changes that omit events can take up to the rescan interval plus bounded scan time to be detected.
- Low-gravity integration supports players and vanilla animals. Custom entities, dropped items, exotic vehicles and other mods' private physics are not rewritten.
- Cryogenic, fuel and air volumes use defined gameplay conversion ratios. No boiling, real gas composition, radiation simulation, orbital mechanics or 1:1 planetary maps are claimed.
- Refuelling on an airless planet needs carried/manufactured crude/coal, imported oxygen or a compatible native source, and power. Ambient lunar/Martian gas is never free breathable air.
- NASA references are used for landform/environment inspiration; optional scientific data preprocessing is external to the mod.
- Empty-dimension suspension affects only owned quarry work/tickets. It cannot promise zero RAM use for empty worlds or override other mods/vanilla.

## Verification record

543 successful recorded test executions (including repeated compatibility runs). See `docs/TESTING.md` and `docs/test-results/2.0.0/checks.json` for individual counts. The final release audit compares the actual archive with all native runtime copies. The test client completed both planetary round trips with inventory/XP preserved, observed higher lunar/Martian jumps, and exercised helmet removal and leak detection in a real generated Moon world. Native server cases verify recipes, exact HDD ticks, separate propellant manufacturing, finite suit/room/chamber air, physical door interlocks, lamp light propagation/reload, optional height-reference bounds, ticket suspension/resume, HBM native tank extraction and ID-conflict isolation. The old quarry, automation and reactor regressions are included. All test worlds are disposable project folders; no existing user world was used. Two separate human clients and every third-party integration are not exhaustively tested.
