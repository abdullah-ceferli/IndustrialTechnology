# Industrial Technology handoff - 2.7.0

## Current expansion: colored Storage Bags (2.7.0)

User asked for bags/colors/capacity like ProjectE and explicitly original ProjectE textures. bag/ package adds BagRegistry, ItemStorageBag, BagData, BagInventory and BagContainer; client/GuiStorageBag uses the unchanged legacy atlas and 255x230 layout. Sixteen metadata variants follow wool order; 104 storage slots, 36 player slots; GUI 8. Registry additions follow consoles, no custom packet. Existing machines and ProjectE contents are not modified.

BagData is overworld WorldSavedData keyed industrialtech_bags, mapping player UUID/color to canonical BagInventory objects. No inventory data lives on items, preventing stale item-copy overwrites. Same-color access shares one object; owner and color isolation remain across death/dimensions. BagContainer validates held slot/color, locks opening hotbar slot, guards number-key/throw/click/shift/drag paths and rejects nested Storage Bags. Standard container packets synchronize NBT stacks; no new tick handler. Recipes: four matching wool/four leather/Ender Chest, plus sixteen dye recipes. Recoloring changes bank selection, not its contents; anvil names do not transfer through crafting.

Seventeen BagServerChecks cover recipes, slots, isolation, click/drag/shift behavior, full/partial stacks, nesting, active-slot protection, death/dimensions, NBT and real JVM persistence. BagClientChecks uses actual item open and vanilla inventory packets, all-color icons and a fully populated 104-slot screenshot. Test runner tools/verify-bag-production.ps1 supports WithProjectE for optional coexistence. Native and client fixtures always use new run-test-* worlds. Rebuild/reobf after runClient before delivery. Current evidence: docs/TESTING.md; user setup: BAG_GUIDE.md.

Original 16 icons and alchchest.png are copied byte-for-byte from the user's installed ProjectE-1.7.10-PE1.10.1.jar into industrialtech paths. Hash/source mapping: development-assets/bags/projecte-assets.json. ProjectE MC17 README/credits included unchanged under META-INF/notices, with attribution in ASSET_CREDITS.md. No ProjectE code or inventory data is bundled/imported.

## Previous patch: monitor screen depth overlap (2.6.1)

User reported broken striped/flickering monitor displays facing left/right. ConsoleBlockRenderer formerly drew stacked full background/frame/glyph rectangles with tiny depth offsets. Replaced these with pure-Java ConsoleScreenMesh: original native palette painted into a 32x32 logical grid, greedily merged once into non-overlapping rectangles, rendered on one exact plane 1/64 block clear of the casing. Shared orientation math handles north/south/east/west with outward winding and upright pixels. Invalid metadata falls back safely. No texture changes, AI assets or continuous renderers. Only this client renderer/new mesh and runtime version change; all machinery/wireless code remains unchanged.

ConsoleScreenMeshTest checks complete single coverage, one exact plane/casing clearance, outward winding in all four directions, and bounded immutable cached meshes. ConsoleFacingClientChecks places both blocks through actual placement-facing logic and captures near/oblique native chunk rendering in all four directions with framebuffer accent visibility and GL checks. Build property consoleFacingClient selects it. Tests use disposable run-test-* worlds, never installed saves. See docs/TESTING.md and docs/CHANGES-2.6.1.md for the final release record.

## Previous expansion: tank and factory consoles (2.6.0)

Final recorded verification: 193 passing checks; production SHA256 `009cde7843eca31b7b1b1658e1114c78591dac5560b494752a15969b7610aef9`. Evidence: docs/test-results/2.6.0.



User requested a history block for linked Green sticks, a Refinery Controller block, battery/capacitor monitoring on Yellow sticks and the new monitor, and no AI textures. New console/ package, client GuiConsole/ConsoleBlockRenderer; see CONSOLE_GUIDE.md and docs/CHANGES-2.6.0.md. GUI 7, packets 11/12. Registry additions follow steel so existing IDs remain stable. New artwork is code geometry on vanilla iron/wool atlas entries; no image generation or new raster assets.

TrendSeries stores 720 long tick/value pairs, gaps -1 and net-rate estimates. ConsoleChannel persists these via paired int-array words for 1.7.10 NBT. History and controller have 16 channels. SensorReading shares MachineInspector for fluid and loaded HBM proxy resolution for genuine HE interfaces; bank snapshots keep full long RF network values. Yellow terminal Controls now includes passive energy telemetry. No energy transfer/conversion.

ConsoleService bounds work to eight console visits per server tick and indexes loaded blocks without chunk tickets. Controller release/refill hysteresis, manual override and exclusive Yellow claims prevent conflicting console rules; active existing Blue programs take precedence. Offline/disabled/mismatched fluid fails closed. Owner/network/dimension/range/window/rate checks guard command packets on ServerWork. Break/unload releases owned outputs. World saves retain history and settings; block items retain settings, omit history and restart paused.

New checks: ConsoleMathTest, ConsoleServerChecks, ConsoleHbmChecks, ConsoleClientChecks; tools/verify-console-production.ps1 uses only fresh run-test-* worlds. Never test in user saves. runClient can leave a development JAR: rebuild/reobf after visual QA before delivering. See docs/TESTING.md for final recorded evidence.

## Previous patch: two HBM upgrades, furnace UI and NEI (2.5.1)

User asked for max two HBM upgrades and the reference legacy blast-furnace GUI, replacing lava with an HBM-style HE bar, plus the factory recipe in NEI when looking up steel. SteelUpgradeProfile is pure bounded upgrade arithmetic. The actual HBM ItemMachineUpgrade type/tier is classified by the optional bridge. Slots 8/9 hold one item each, with Speed/Power/Effectiveness/Overdrive I-III and combined type cap III. Original inventory indexes remain for save compatibility; usable IDs are 0 iron, 2 dust, 4 output, 8/9 upgrades. IDs 1/3/5/6/7 are retired overflow from old saves only, noninsertable and absent from GUI/pipes. drainLegacy moves their contents into the visible inputs/output before processing, preserving old materials. Container GUI indices 0..4 map through SteelContainer.INVENTORY; player inventory starts at 5. Recipe profile locks on first paid tick and is saved as BatchTicks/BatchHE/BatchCoal; legacy NBT uses the base profile for unfinished work. HE intake is 20k/t to sustain all upgrade combinations.

Latest user correction removes buffers and the connector beside the upgrades. GuiSteelFactory and SteelGuiArt compose unmodified HBM atlas regions in a compact 204x196 screen: coal above iron, flame to their left, progress arrow and large output, electrical bar at the far right, with two upgrade slots below it separated by small gaps. Flame and progress overlays exactly share base sprite positions. Framebuffer checks cover zero/half/full arrow and prohibit drawing beyond its base. Packet IDs unchanged; bounded HE, progress, duration, HE/t and coal requirement use standard container progress words. NEI discovers integration/nei/NEIIndustrialTechnologyConfig; BulkSteelRecipeHandler registers both recipe and usage lookup. NEI dependencies are compileOnly, with no explicit reference from ordinary client/server startup. See STEEL_FACTORY_GUIDE.md and docs/TESTING.md.

Current 2.5.1 validation: 160 passing recorded checks; production SHA256 `60cc4bf2092e062fdf1210bf5ddf1cd50ba36463e9df723c5f355c07d13d704b`. Evidence and final UI snapshots: docs/test-results/2.5.1. Final layout: upgrade items at (174,76) and (174,96), energy at (174,18), screen 204x196. Arrow uses a code mask of the original gray arrow spans so its white fill cannot spill beyond the shape; sprite rendering resets color after slot borders.

## Previous expansion (2.5.0)

## Current update: bulk HBM steel and bank colors (2.5.0)

The user clarified bank input green, output red, full charge gold; no portable battery. Added steel/ package and GuiSteelFactory, GUI ID 6. Optional HBM bridge/subclass implement the installed X5771 HE API, compileOnly from .toolchain/hbm-api/HBM-X5771-dev.jar. Registry additions come after RailwayRegistry; existing IDs stay stable. Factory runs 16 iron + 4 coal dust -> 16 genuine HBM steel / 100 ticks / 500 HE per working tick, 1M HE capacity. All six faces accept validated ingredients and extract outputs only; AE2 real InventoryAdaptor and real Thermal servo/itemduct routes are covered. Save and real JVM restart preserve paid partial work. Expensive core and factory recipes are in STEEL_FACTORY_GUIDE.md.

Latest visual request: use HBM Nuclear Blast Furnace (LEGACY). BlockSteelFactory resolves its five original texture names in the installed HBM atlas; no copied HBM assets. Metadata low 3 bits are facing and bit 8 is actual working state; changes never replace the tile. GuiSteelFactory uses standard ICrafting progress words for its bounded HE buffer and normal inventory packets. Capacitor side-mode colors/glyphs and full-charge display changed; original casing textures stay byte-identical. BankManager reserves meter level 16 for exact full capacity. Other bank ledger/transfer logic is unchanged.

No user worlds were edited. Executed evidence and distribution identity: docs/TESTING.md and docs/test-results/2.5.0. Do not ship the dev JAR left by runClient; build/reobf after client QA.

Historical 2.5.0 validation: 161 passing recorded checks; SHA256 `feada01d7d32489a2608617dd73f30deabd386c9aa8e6b9d630641beb69d761c`. Exact production bytes passed the HBM/AE2/Thermal server, standalone server and bank regression worlds. Screenshots and machine-readable release identity are archived in docs/test-results/2.5.0.

## Previous patch (2.4.2)

## Current patch: capacitor item stacking (2.4.2)

Empty bank items stack to 64, charged items stay at one. Default empty drop NBT is normalized to match crafted banks; player inventory ticks normalize legacy default-empty items. Configured empty stack placement restores its saved settings. Charged restoration still requires a single item. Item settings revision, non-default settings and foreign/custom-name tags are preserved. No bank ledger/network changes. See docs/CHANGES-2.4.2.md and docs/TESTING.md.

Historical 2.4.2 validation: 128 passing checks; SHA256 `bc3b34c176801c32285ba54fc52de47ded1a8cedaa95417aa8bd8e7dafe58fab`. See docs/TESTING.md and docs/test-results/2.4.2.

## Previous patch: solar panels have no UI (2.4.1)

Removed panel activation and the server/client GUI ID 3 routes. ID 3 remains reserved; railway GUIs 4/5 retain their IDs. Removed the solar presentation/container mode. Solar generation, RF, array caching, textures, recipes and saved tile data are unchanged. Current guide: SOLAR_AND_RAILWAY_GUIDE.md. Previous 2.4.0 evidence remains archived; current patch verification is recorded in docs/TESTING.md.

Actual checkout: `D:\IndustrialTechnology\industrialMOD` (the configured `D:\industrialMOD` path is stale). Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8. Mod ID remains `industrialtech`.

Current patch validation: 144 passing checks. Production SHA256 `1851a14260605de1a0fa2980e1d7f12e024e8d15791220f65bcb1f91e0592721`. See docs/test-results/2.4.1 and docs/TESTING.md.

## Current expansion: solar and railway (2.4.0)

See SOLAR_AND_RAILWAY_GUIDE.md, ASSET_CREDITS.md and docs/CHANGES-2.4.0.md. New packages solar/, railway/, expansion/ plus legacy client renderers; registry additions come after the bank. Common GUI IDs 3/4/5 and packet IDs 9/10. SolarArrays caches loaded horizontal components and reuses the existing unchanged BankLedger for physical RF ownership/reservations. Trains use measured TrackWorld cursors, original OBJ meshes, server physics, logical counted cargo, UUID coupling, loaded-entity indexing, entity-bound Forge tickets and station adapters. RailwayTickets is handled before existing machine tickets in the shared callback. Tools/create_railway_assets.py preserves reproducible model sources. Disposable native/client tests are RailServerChecks and RailClientChecks; never use installed saves as fixtures. Final executed evidence is recorded in docs/TESTING.md.

Final 2.4.0 validation: 533 recorded passing checks (101 unit, 415 native, 17 client), archived in docs/test-results/2.4.0. Production SHA256 `8b50576a3f61e0e34e3e0d8e36a71a4b6e5f26ff84df5cc8472bca8b5b86d650`. The native matrix runs exactly these bytes; see docs/TESTING.md for coverage and its limits.

## Previous patch: capacitor default appearance (2.3.1)

The user correctly identified 2.3.0's permanent front as the optional display. The exterior atlas
was also incorrectly stretched as if its four alternative strips were one face texture. Defaults now
show the normal plain casing, including old 2.3.0 saves. Sneak + right-click with an empty hand toggles
the clicked Both face's meter. `DisplayFaces` is a local six-bit NBT field retained in item drops and
visual packets. No ledger, network or RF logic is changed. The meter now samples the original SINGLE
atlas quarter at 0.25 width and 0.8 height. Inventory rendering stays plain; hidden meters suppress
unnecessary charge redraw packets. See `docs/CHANGES-2.3.1.md` and current test evidence.

Historical 2.3.1 validation: 114 successful recorded checks (78 unit, 20 native initial,
1 genuine JVM restart, 15 Ender IO/NEI client). The exact distribution is sealed in
`.toolchain/capacitor-release-2.3.1/`; its only payload differences from 2.3.0 are three capacitor
classes and version metadata. All texture bytes and RF/network/other-machine classes remain identical.
The comparison screenshots show our banks on the left and actual Ender IO banks on the right.

## Previous update: Advanced Capacitor Bank (2.3.0)

The latest task adds one block `industrialtech:advanced_capacitor_bank` after all existing registrations.
Each bank contributes 25M RF and shared 25k RF/t input/output. `capacitor/BankLedger` keeps per-block
balances and same-tick quota ownership; `BankManager` caches loaded six-face components, RF receivers
and redstone counts. No chunk forcing or per-tick graph searches. `LoadedBankAccess` prevents vanilla
redstone queries from incidentally loading a neighbor chunk. Harvested items retain only their own RF.
The GUI and server-authoritative commands use GUI ID 2 and packet IDs 7/8; existing IDs stay stable.

The user explicitly changed the artwork request on Oct 24: use the exact Ender IO Vibrant textures for
personal play. Seven PNGs are copied byte-for-byte from their installed 2.3.0.430_beta JAR. Original
LICENSE, NOTICE and attribution are bundled. `client/CapacitorBankRenderer` composes them in cached
chunk meshes with connected borders and the original cyan gauge, with no TileEntity renderer.
The earlier authored artwork is archived in `.toolchain/authored-capacitor-before-EnderIO.zip`.
`tools/create_capacitor_assets.py` now regenerates only the independent GUI atlas.

Setup, recipe, config, split/merge settings policy and honest limitations: `docs/CAPACITOR_BANK.md`.
Prior 2.3.0 release: all 78 unit tests and the archived native/client suites pass. Four exact-JAR native configurations each pass an actual JVM restart. Current counts and tested limits are in `docs/TESTING.md`; the audit checks original-bytecode preservation and exact texture provenance.

Tests: `BankLedgerTest`, `BankServerChecks`, `BankPipeChecks`, `BankClientChecks`; the harness is
separate from the distribution JAR. Native runner: `tools/verify-capacitor-production.ps1`.
Never install a dev JAR: runClient rewrites build/libs; rebuild with `-PproductionTest` after client QA.
Pre-update backup: `.toolchain/pre-capacitor-2.2.1/`. Installed game and user worlds are not modified.

## Retained removal/migration context

The user requested removal of the entire space expansion, then explicitly asked to **keep the green block and allow a lever on it**. The retained block is the Green Redstone Lamp. Its old registry name and four-block crafting recipe remain. Version 2.2.1 replaces its decorative textures with plain solid green faces: darker green off, bright green on. Regenerate these two flat tiles with `tools/create_lamp_assets.py`. Its six faces now support vanilla levers and torches even though its material is glass. Redstone controls actual light 0/15.

Space Java/client code, providers, dimensions, machines, suits, rockets, launchpads, minerals, recipes, fluids, artwork and live instructions are removed. The exact pre-space edits in eight original classes were restored from the verified 1.6.3 baseline; all other original quarry/wireless/reactor code remains. Space-only chunk-loading suspension was removed. The compile-only HBM development dependency is no longer needed; optional reactor integration remains.

`core/ContentMigration.java` accepts only the exact retired registry names, allowing a previous 2.x Overworld to load without the removed-content prompt. Existing green lamps survive. `core/RetiredDimensionRecovery.java` corrects old Moon/Mars player NBT before the login packet, preserving inventory/XP. It reads existing legacy configured IDs without creating or saving a space config; registered dimensions are left alone. Removed space contents disappear when the user opens and saves a world; old world/dimension/config files are not deleted. See `docs/CHANGES-2.2.0.md`.

Previous 2.1.0 source/docs and JARs were backed up before deletion in `.toolchain/pre-space-removal-2.1.0/`. Earlier test worlds and versioned evidence remain as history. Never delete or use the human user's actual Minecraft worlds as fixtures. No automatic installation into the user's game is part of this update.

Build: `tools/build.ps1`; distribution: `build/libs/IndustrialTechnology-1.7.10-2.3.1.jar`. Current validation is in `docs/TESTING.md` and `docs/test-results/2.3.1/`. The original automation reference is `AUTOMATION_CODES.md`; reactor instructions remain in `docs/REACTORS.md`.
