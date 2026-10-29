# Asset credits — Industrial Technology 2.4.0

## Current Ender IO photovoltaic assets

Source: [Team-EnderIO/EnderIO](https://github.com/Team-EnderIO/EnderIO), branch **26.3**, inspected 2026-10-24, revision **9ec8183274d61b57ae0562c5d9b4bd486fef8f0c**. The supported default 1.21.1 branch was also inspected before checking the newer branches. The final textures are from the newest inspected 26.3 branch, not the installed legacy Ender IO JAR.

Credit: Team Ender IO and contributors; the upstream photovoltaic template credits Blockbench. The repository's LICENSE.txt is the **Unlicense**, permitting copying/adaptation/distribution. Its unchanged license is packaged as META-INF/licenses/EnderIO-Modern-Unlicense.txt. Private use is not relied on as a licensing exception.

Copied unchanged from `enderio/src/main/resources/assets/enderio/textures/block/`:

* energetic_photovoltaic_module_top.png and energetic_photovoltaic_module_side.png -> solar_0_top.png / solar_0_side.png;
* pulsating_photovoltaic_module_top.png and pulsating_photovoltaic_module_side.png -> solar_1_top.png / solar_1_side.png;
* vibrant_photovoltaic_module_top.png and vibrant_photovoltaic_module_side.png -> solar_2_top.png / solar_2_side.png, also the unchanged cell surface/backing for tiers 4 and 5.

The latest `models/block/template_photovoltaic_module_base.json` describes a 16 x 2 x 16 cell with side UVs sampling the first two pixels. The legacy renderer recreates that geometry/UV arrangement and adds original connected tier frames. Modern blockstate/Java code is not bundled. Pinned originals and model reference: development-assets/solar/enderio-26.3. Earlier inspection originals are retained separately for provenance.

## Locomotives, wagons, tracks and UI

All train OBJ meshes, cab interiors, wheels, rods, bogies, container fittings, track geometry, 256 x 256 material atlas, item icons, station faces, tier frames and new GUI artwork are original project work. No Traincraft, Railcraft, Immersive Railroading, HBM or Siemens/Alstom model or livery texture is copied. No real operator logo is applied. Source metres/UVs and reproducible generator are preserved in development-assets/railway and tools/create_railway_assets.py.

Photographic/engineering references, used for proportions and details only:

* [Alstom: first Prima T8 freight locomotive for Azerbaijan](https://www.alstom.com/press-releases-news/2018/6/alstom-presents-first-freight-locomotive-for-azerbaijan). The manufacturer's reference photograph is stored only in development-assets/railway/references for development inspection, not packaged in binary or sources JARs. Copyright remains with its owner. The gameplay model uses an original red/white/gray scheme and a shared standard gauge.
* Siemens' ACS64 engineering brochure, [manufacturer-authored technical sheet](https://img1.wikia.nocookie.net/__cb20150901000731/obts/images/9/9f/2013.xBBx~LOxx.AARx_1m435~0020m32_Siem.CS64.PDF), for the approximately 20.32 m body, two bogies and passenger locomotive profile. No photographs or trademarks are mapped onto the mesh.
* [Traincraft source/license information](https://github.com/Traincraft/Traincraft), [Traincraft 1.7.10 downloads](https://traincraft.org/download/) and [Immersive Railroading source](https://github.com/TeamOpenIndustry/ImmersiveRailroading) were inspected for the legacy railway integration decision; none of their code/assets is copied.

Blender was not installed in this environment. Meshes were authored as component geometry and exported directly to OBJ for the native legacy renderer, rather than presented as Blender output. Original sound synthesis has WAV masters and Ogg Vorbis runtime files; these are approximations, not recordings of the named real locomotives. The project-local FFmpeg encoder is a build tool and is not shipped in the mod JAR.

## Existing assets and code

The seven existing capacitor textures retain their earlier attribution to CrazyPants and Ender IO contributors, sourced unchanged from the user's installed EnderIO-1.7.10-2.3.0.430_beta.jar. Its Apache-2.0 license and earlier provenance remain packaged. This legacy bank artwork has a different upstream revision/license from the modern solar artwork.

CoFH's bundled legacy RF API retains its LGPLv3 license and source provenance. See THIRD_PARTY.md. Other pre-existing project textures are preserved. No Minecraft, HBM, rail-mod JAR, manufacturer photograph or audio-converter executable is bundled into the release.

## HBM steel factory appearance (2.5.0)

At the user's request, the Bulk Steel Factory uses the installed HBM Nuclear Blast Furnace (LEGACY) texture atlas entries `rtg_difurnace_front_off/on`, `rtg_difurnace_top_off/on`, and `machine_rtg_furnace_side_alt`. These are runtime references to HBM's own assets, not copies bundled in Industrial Technology. HBM is by HBMTheBobcat and contributors: https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT (LGPL-3.0). Capacitor casing assets and their existing attribution remain unchanged; the new input/output glyphs and gold overlays are code-drawn.

HBM 2.5.1 GUI integration also references the installed `textures/gui/GUIDiFurnace.png` and `textures/gui/processing/gui_electric_furnace.png` atlases for the gray blast-furnace layout and electrical energy meter. No HBM image files are copied or edited.


## Monitoring and controller consoles (2.6.0)

Both new case displays and all GUI elements are original code-drawn pixel geometry. They reference Minecraft's existing iron/wool atlas entries at runtime; no Minecraft asset is copied into the mod. No new raster textures, generated images, downloaded artwork or continuously animated block renderers are added.


## ProjectE Storage Bag assets (2.7.0)

At the user's explicit request for their private build, the sixteen original bag icons and original gray bag/chest inventory atlas are copied unchanged from their installed `ProjectE-1.7.10-PE1.10.1.jar`. Source icons: `assets/projecte/textures/items/alchemy_bags/*.png`; source GUI: `assets/projecte/textures/gui/alchchest.png`. Destination namespace: `industrialtech/textures/items/bags/` and `industrialtech/textures/gui/storage_bag.png`. Light Blue/Light Gray filenames are normalized from lightBlue/silver without changing image bytes. All hashes and mappings are recorded in development-assets/bags/projecte-assets.json.

ProjectE is by sinkillerj, Moze_Intel and contributors. Upstream legacy credits identify Magic Banana for original ProjectE textures, x3n0ph0b3 for EE2 assets, and MidnightLightning for EE2 GUI textures. Reference and unchanged notice: [ProjectE MC17 README](https://github.com/sinkillerj/ProjectE/blob/e4e764bde69112b752a98dc5085a7abfbfd3ee13/README.md), packaged as META-INF/notices/ProjectE-MC17-README.md. The legacy branch's redistribution statement is preserved in that notice; a separate permissive license for individual legacy image files was not present and is not asserted here. This records the requested private asset provenance rather than representing the artwork as original or AI-created. No ProjectE source/classes, complete mod JAR or player inventories are copied.
