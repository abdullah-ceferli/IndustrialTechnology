# Industrial Technology — Space Exploration

Minecraft 1.7.10 · Forge 10.13.4.1614 · Java 8. This expansion belongs to the existing `industrialtech` mod. Existing quarry, loader, wireless, reactor and automation registry names remain unchanged.

## Getting started

1. Manufacture steel, titanium, thermal alloy and electronics. HBM's actual registered steel/titanium ingots and plates are used when available. Without HBM, expensive standalone material recipes provide a route forward.
2. Craft a Research Computer, Telescope and Research HDD. Place the telescope directly beside the computer, under unobstructed sky. Both machines belong to the player who places them. Supply the computer with RF.
3. Put the HDD in computer slot 1. Choose **Research Moon** (200 powered ticks, 10 seconds at 20 TPS). **Research Mars** requires completed Moon data and 6,000 powered ticks (5 minutes at 20 TPS).
4. Manufacture the 11 rocket parts, build an Assembly Station, and supply 50,000 RF. Put the parts into their assigned slots and press **Assemble**. Missing part numbers appear on screen; hovering an empty slot shows its name.
5. New Assembly Stations default to **Space Shuttle**. Use **Type** to switch between Shuttle and the existing smaller rocket. For the Shuttle, craft and deploy the **LC-39 Shuttle Launch Facility Kit**, then insert the finished stack and HDD into its root controller. The smaller rocket still uses the original 7 x 7 pad.
6. Equip and refill a complete spacesuit. Bring base materials, an RF source, breathable air, and return propellant. The Moon and Mars cannot supply breathable ambient air.

Research progress is stored on the removable HDD. Removing it, losing RF, obstructing/removing the telescope, or unloading the computer's chunk pauses research. Progress resumes from the same drive. A computer never grants research globally to all players.

## Rocket assembly slots

| Slot | Required real component |
|---|---|
| 1 | Rocket Engine |
| 2 | Rocket Fuel Tank |
| 3 | Oxidizer Tank |
| 4 | Guidance Computer |
| 5 | Navigation Electronics |
| 6 | Reinforced Rocket Hull |
| 7 | Rocket Fins |
| 8 | Rocket Nozzle |
| 9 | Crew Capsule |
| 10 | Heat Shield |
| 11 | Landing Legs |
| 12 | Finished rocket output |

Each part has a crafting recipe. The engine requires a nozzle, electronics, advanced materials and alloy; the guidance computer requires electronics; the capsule requires guidance and a heat shield. The assembled rocket has no shortcut crafting recipe.

## Space Shuttle and LC-39 facility (2.1.0)

The main dimensions use **one Minecraft block per metre**: 56.1 m full stack, 37.2 m orbiter, 23.8 m wingspan; 119 x 99 m concrete hardstand, 49 x 41 x 8 m mobile platform, 75 m steel service tower with lightning mast reaching 106 m. Small construction details are voxel approximations. The facility includes six platform supports, exhaust openings/deflectors, a fixed service tower, a parked rotating-service framework, a climbable ladder and retracting access arms.

1. Craft eight **Launch Facility Sections**. Each section uses four iron blocks, four existing Rocket Launchpad blocks and one Airtight Hull. Combine the eight sections around one Launch Controller to make a single facility kit.
2. Find a large, level, clear area. Place the kit on the **top of solid ground**, facing the desired direction. The controller appears two blocks above the clicked ground; the hardstand occupies the air layer just above ground. Most of the site extends behind the controller. It needs 59 m to each side, 74 m behind, 24 m forward, and 106 m above the controller base. Rotate before placing. Ground level must be at most Y147.
3. Stay in the world while deployment scans and then places blocks in batches. The root controller shows progress. Occupied/protected positions cancel deployment, recover built parts, and return one kit. Existing builds are never cleared to make room. Do not place a second kit inside an existing facility.
4. Open the controller or right-click a facility part. Put the assembled **Space Shuttle Stack** in slot 1 and the researched HDD in slot 2. Supply kerosene, liquid oxygen and cabin air. The Shuttle appears on the mobile platform. The server checks its 27 x 30 x 57 m clearance envelope again before launch.
5. Press **Board & launch**. Access arms swing away during countdown. The full stack ascends, travels and descends using the existing mission system. The cockpit boarding point is elevated; passengers and GUI range account for its height.

New stations default to Shuttle; stations saved before 2.1.0 retain their smaller-rocket selection until changed. All original rocket IDs and saved smaller vehicles remain usable.

Breaking a facility part as its owner requests dismantling of the **whole structure**. Recovery runs in batches, respects protection cancellations, and returns one kit carrying the controller's inventory, settings and stored fluids. Other players cannot dismantle it through its normal break/use path. Construction and recovery resume after save/restart. External damage also requests recovery; parts have no separate item drops. Keep the area loaded and a player in the dimension while recovery completes. Protection mods can pause recovery until removal is permitted.

The real Shuttle flew in Earth orbit and its main engines used liquid hydrogen/oxygen. This mod deliberately retains the requested **kerosene/LOX** supply and Moon/Mars travel, vertical stack touchdown and reusable stack as gameplay abstractions. It does not simulate historical Shuttle staging, gliding runway recovery or full-scale launch-complex plumbing.

## Smaller rocket launchpad and shared controls

Build a flat **7 × 7** layer of Rocket Launchpad blocks. Replace its centre block with the Launch Controller. A Fuel Port may replace another pad block and forwards the three supported supplies to an owned controller within three blocks on the same level. Clear a **5 × 5** column for **16 blocks above** the controller.

Controller inventory: **slot 1 = assembled rocket; slot 2 = researched HDD**. Select Moon, Mars or recorded Earth departure site. **Board & launch** validates the physical pad, vehicle, navigation, both propellants, ownership and clear space, then boards the player before countdown. Dismounting during countdown cancels it without burning propellant.

Default mission costs, in mB:

| Leg | Kerosene | Liquid oxygen |
|---|---:|---:|
| Earth ↔ Moon | 12,000 | 24,000 |
| Any leg involving Mars | 24,000 | 48,000 |
| Earth → Moon, carrying return reserve | 24,000 | 48,000 |
| Earth → Mars, carrying return reserve | 48,000 | 96,000 |

Return reserve is enabled by default for outward trips. Switching it off explicitly displays **ONE WAY**. Carrying only outward fuel can strand a player until they refuel. Earth return uses the recorded departure region and needs one remaining leg. Moon → Mars also costs a Mars leg; the initial Moon round-trip reserve alone does not cover that detour.

The rocket counts down, ignites, accelerates upward, changes dimension, and descends to actual terrain. Fuel is spent once at ignition. The same player travels with inventory, XP and equipment. A bounded terrain search cancels departure when it cannot find a landing site; no floating landing platform is generated.

After landing, right-click the rocket for its return planner. Its landing legs support departure from the landing site without constructing a second Earth launchpad. The hull, HDD and unspent fluids remain in the vehicle. Sneak-right-click a landed rocket with an empty hand to recover it as an item. A held Space Canister instead transfers fuel/air: normal right-click supplies the landed vehicle, and sneak-right-click withdraws fluid without recovering the rocket. Placing that recovered rocket in another controller restores its stored HDD and fluids once, when the controller has sufficient tank space and an empty HDD slot. An active vehicle identity cannot be launched as a second entity.

The third vehicle tank accepts breathable air for a finite pressurized cabin supply (1 mB per passenger-second). Without cabin air, suit air is used in space. It is separate from propellant oxygen.

## Manufacturing and fluid handling

All manufacturing uses real RF buffers through the CoFH 1.7.10 energy API. Default processing draws **200 RF/t**. Existing RF generators and compatible cables can power these machines. Native HBM HE is a different energy system; this expansion does not pretend it is RF.

| Machine | Input and powered process | Output |
|---|---|---|
| Fuel Refinery | 1 coal in slot 3; 100 ticks | 1,000 mB crude oil |
| Fuel Refinery | 250 mB crude oil; 100 ticks | 150 mB kerosene |
| Oxygen Plant | Earth atmospheric separation; 100 ticks | 500 mB oxygen gas |
| Oxygen Plant | 250 mB oxygen gas; 100 ticks | 250 mB liquid oxygen |
| Air Compressor | Earth air + filter in slot 3; 100 ticks | 1,000 mB breathable air; 1 filter wear |
| Suit Refill Station | Suit chest pack in slot 1 + breathable air | 20 breathing ticks per mB |

A filter lasts 256 batches. The oxygen plant's separation and liquefaction are different stages. On the Moon/Mars it needs an imported oxygen supply for liquefaction. The refinery can operate from carried coal or crude oil in space. The air compressor refuses lunar and Martian ambient gas. Pure oxygen, kerosene, HBM AIRBLAST and arbitrary gases are never accepted as breathable air.

Space Canisters hold **1,000 mB**. Right-click a machine with a filled canister to supply it; sneak-right-click to withdraw stored fluid. Most processing machines also accept a fluid-container item in slot 1: it empties accepted input, or fills with manufactured output when empty. Forge-compatible fluid pipes can fill inputs and extract manufactured outputs. The refinery's output is kerosene; the oxygen plant's output is LOX; the compressor's output is breathable air. Cryogenic LOX is registered at 90 K, so use piping rated for cold fluids when another mod models pipe temperatures.

## HBM compatibility

The LC-39 **root controller** and existing **Fuel Port** implement HBM's native MK2 fluid receiver when HBM is loaded. Connect real HBM fluid pipes to exposed faces of that block. Decorative facility beams and platform blocks are not fluid inputs. The root is the controller block where you placed the kit; it is one block above the concrete hardstand. Two horizontal faces can therefore receive separate pipe lines.

- Set one pipe line to HBM **KEROSENE** and the other to HBM **OXYGEN**. This HBM version marks OXYGEN as cryogenic liquid; it fills the pad's **liquid oxygen** tank directly. AIR fills its separate breathable-air tank.
- Set the source HBM storage tank to a mode that **sends** fluid. Connect from its actual fluid port. Native pipes must have the matching fluid type; differently typed lines remain separate.
- The receiver accepts at most 1,000 mB per server tick across native pipe networks. The source or pipe may limit the actual rate further. Full tanks, disabled receivers, broken/unloaded connections, wrong fluids/pressure and denied interactions reject transfer.
- A Fuel Port can still forward supplies to an owned launch controller within three blocks on the same Y level. Forge fluid pipes and Space Canisters remain supported. Directly adjacent sending HBM small storage tanks can also supply inspected fluid-capable ports.

The optional API is compiled against X5771 and stripped by Forge when HBM is absent; no HBM classes or JARs are packaged in the mod. Native HBM HE is separate from RF.

**Planetary metals are usable in HBM crafting.** Smelt lunar iron rock or Martian hematite into vanilla iron ingots. Smelt lunar ilmenite or Martian titanium-bearing rock into titanium concentrate, then craft nine concentrate into **HBM's actual titanium ingot**. Smelt lunar feldspar into aluminium concentrate; nine craft into **HBM's actual aluminium ingot**. Martian silica smelts into vanilla quartz. These exact item identities work in recipes that demand the native items, not only ore-dictionary recipes. Without HBM, standalone titanium/aluminium outputs are used. Rock blocks also register conventional iron, titanium, aluminium and quartz ore names for compatible machines; arbitrary machine-specific processing recipes are not invented for every mod.

## Moon and Mars

The Moon contains regolith, dust, basalt maria, highland rock, breccia, craters with bowls/rims, ridges, scattered boulders and regional subsurface ice. Mars contains red/brown regolith, layered rock, craters, dry channels, valleys, dunes and regional ice. A scaled volcanic province is centred near **X 8192, Z 8192**; a meandering canyon province follows roughly **Z 3000**. Polar ice regions begin beyond approximately **|Z| 18,000 on the Moon / 20,000 on Mars**.

Newly generated chunks also contain body-specific mineral deposits:

| Body | Mineable mineral-bearing rock | Useful product |
|---|---|---|
| Moon | Iron-bearing rock | Iron |
| Moon | Ilmenite, enriched in maria | Titanium concentrate (Fe/Ti-bearing source) |
| Moon | Feldspar, enriched in highlands | Aluminium concentrate |
| Mars | Hematite | Iron |
| Mars | Titanium-bearing rock | Titanium concentrate |
| Mars | Silica-rich rock | Quartz |
| Both, regional cold deposits | Subsurface ice | Water: craft one ice block + one bucket + one coal into a water bucket |

These represent measured geological constituents; vein sizes, ore grades and simple furnace/crafting extraction are gameplay abstractions. They are not claims of giant pure-metal seams or industrially easy extraction. Ice distribution remains regional. Existing generated chunks are preserved, with **no retrogen**; explore new chunks to find the new minerals. Resource generation and density are configurable in the space config's `resources` section.

These are deterministic procedural landscapes informed by NASA descriptions and topographic morphology, **not 1:1 reconstructions or bundled NASA elevation data**. Reference sources and optional height-reference import are described in `SPACE_TECHNICAL_NOTES.md`.

Moon gravity is **0.165 g**; Mars is **0.38 g**. Supported players and vanilla animals have lower airborne acceleration and adjusted fall damage. Earth is unchanged. Custom vehicles and exotic mod entities retain their own physics. A black lunar sky and dusty Martian sky have separate renderers; regional Mars dust haze/particles are configurable.

Earth appears in a fixed region of the near-side lunar sky. The flat-world approximation treats X as longitude repeating every 65,536 blocks, with X≈0 on the near side and X≈32,768 on the far side; Z approximates latitude. Earth is suppressed on the far side. This is an explicit cartographic approximation of synchronous rotation. Lunar and Martian day lengths are configurable.

## Suits, air and warnings

Craft all four matching suit pieces: helmet, chest pack, leggings and boots. Partial/broken sets do not provide a pressure seal. Three tiers have default air capacities **10 / 30 / 90 minutes**; the lowest equipped tier governs protection. Chest-pack NBT stores real breathing time and follows the item through inventories and saves. Higher tiers have more durability and slower thermal wear. Tier colours, visor and backpack distinguish the suits.

Outside a pressurized room/cabin, a complete suit consumes air every server second. Air stops being consumed on Earth and in breathable interiors. The HUD shows air time, pressure, gravity, approximate ambient temperature in °C, dust and warnings. Air-low warnings appear before depletion. Missing protection or empty air starts a grace period followed by gradual damage. A finite refill station is needed; there is no free vacuum-air refill.

## Pressurized bases

Build a sealed room using full solid blocks or the supplied Airtight Hull/Glass. Partial blocks, gaps, fluids and open doors are not airtight. Install an Atmosphere Pressurizer in the floor: **the air block directly ABOVE it is the room probe**. Supply RF and breathable air.

The GUI shows seal status, pressure, room volume, stored air, upkeep and leak state. Nominal room fill needs **50 mB per interior block**, limited to **500 mB/second**. A sealed room consumes small maintenance losses plus occupant breathing, including while unpowered or paused. Pressure of **80% or more** is breathable, allowing safe helmet removal. More than one pressurizer does not multiply pressure freely in the same room.

Room scans are incremental, limited in radius/volume and cached. Unloaded boundaries are not treated as sealed. Building/breaking, doors and chunk unloading invalidate the cache; periodic validation catches mod changes that omit Forge block events. Wall/door breaches stop pressure maintenance and cause gradual loss. The previous room remains the affected pressure volume while gas escapes. There is no TNT explosion effect.

Default supported room limits: **4,096 interior blocks**, **32-block probe radius**, **256 scan nodes per room/tick**, **1,024 scan nodes globally/tick**. Larger/open spaces report a boundary/limit problem. RF and air machines remain normal loaded-chunk machines.

## Functional airlock

This implementation uses a fixed **1 × 3 × 2 interior chamber**, with a **3 × 5 × 4 outer envelope** including walls, floor and roof. Orient it on the Z axis.

```text
Top view of the wall layer (not the floor):
          base interior (-Z)
             # I #       I = two-block inner door, Z -2
             # . #       . = air in the chamber
             # . #       controller in floor beneath this cell, Z 0
             # . #
             # O #       O = two-block outer door, Z +2
          outside (+Z)
```

Install the controller in the chamber's middle floor block. Door bottoms are one block above the controller; each supplied Pressure Door places its upper half automatically. Finish solid floor, side walls and ceiling. Put a separately pressurized base behind the inner door at -Z. Feed the airlock controller RF and breathable air. Right-click either physical door to open its controller screen.

**Enter from vacuum:** open outer door at outside pressure → walk into chamber → close both → **To base** → wait for pressure match → open inner door → walk into base → close inner door.

**Exit:** open inner door at base pressure → walk into chamber → close both → **To outside** → wait for pressure match → open outer door → walk outside → close outer door.

The GUI displays chamber/base/outside pressure and both actual door states. A door cannot open while the other is open or while pressure differs by more than 5%. Cycling closes both first, refuses to crush an entity standing in a doorway, consumes RF/air and recovers up to 80% of chamber air when depressurizing. Missing structure leaks rather than preserving magical pressure. Loss of power prevents cycling.

## Empty-dimension optimisation and green lamp

`pauseOwnedChunkLoadingInUnoccupiedDimensions=true` is the default. After the last player leaves Earth and the grace/travel exemption expires, this mod releases its quarry/loader tickets and pauses quarry work. Settings and progress remain saved. On return, a persistent machine index restores valid machines gradually, at two owner chunks per tick. One remaining player prevents suspension. Planet suspension is separately configurable. Other mods' tickets and vanilla spawn-chunk rules are not changed; this does not guarantee that Minecraft unloads every empty dimension.

The **Green Redstone Lamp** is independent of the space progression. Craft four from glass, green dye, glowstone and redstone. It has muted green appearance and light 0 without power; powered appearance emits light 15. No blinking or tile-entity tick loop.

## Configuration and verification

Settings are in `config/industrialtech-space.cfg`: dimension/biome IDs, research ticks, mission volumes, suit capacities, day lengths, atmosphere budgets, particle limit and idle policy. Keep dimension/biome IDs stable for a save and consistent between client and server. Conflicts disable space registration/travel instead of replacing another mod's dimensions.

Development/test reports and limitations are recorded in `SPACE_TECHNICAL_NOTES.md`. Tests use fresh disposable project directories, not the user's existing worlds.
