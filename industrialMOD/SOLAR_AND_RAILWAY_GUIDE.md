# Solar and Railway Guide вЂ” Industrial Technology 2.4.1

Minecraft 1.7.10, Forge 10.13.4.1614, Java 8. This expansion is standalone. Install the same ordinary release JAR on client and server; replace the previous Industrial Technology JAR. Keep only one version. The sources and test harness JARs are development files.

## Five photovoltaic tiers

| Tier | Name | Clear-noon maximum |
|---|---|---:|
| 1 | Photovoltaic Cell | 10 RF/t |
| 2 | Advanced Photovoltaic Cell | 40 RF/t |
| 3 | Vibrant Photovoltaic Cell | 160 RF/t |
| 4 | Enhanced Photovoltaic Cell | 1,280 RF/t |
| 5 | Ultimate Photovoltaic Cell | 5,120 RF/t |

Place panels outdoors with the cell surface facing up. Output follows a daylight sine curve, peaks at noon and reaches zero at night. An opaque roof blocks generation. Rain reduces it to approximately 35%; thunderstorms to 10%. Sky visibility is cached for up to 20 ticks, so a distant roof change may take one second to register. Dimensions without a sky generate nothing. Values and weather multipliers are configurable.

Panels connect horizontally into arbitrary shapes. Mixed tiers can join by default; disable `mixedTiers` for separate-tier networks. Arrays have shared output and a cached connection graph. Connect an RF cable, bank or machine below or beside any member. The top face is the photovoltaic surface. Every panel keeps its own physical buffer; splitting an array does not copy its energy. Arrays never force chunks to load. A panel removed from the world loses its small generator buffer.

Solar panels work automatically and have no GUI. Right-click does not open a screen or intercept normal item placement. Frame colors distinguish all five tiers, including the two new tiers. The official current Ender IO cell textures are adapted to a thin 1/8-block legacy mesh; connected interior frames disappear. See ASSET_CREDITS.md for upstream revision and license.

Four Ultimate panels produce **20,480 RF/t at clear noon**. At 20 TPS this can sustain a properly supplied Quarry at its existing 20,000 RF/t / 100 blocks per second target. Output falls away from noon, in poor weather and at night; use capacitors and additional panels for continuous operation. This does not change quarry energy costs or world tick speed.

## Build a railway

All rolling stock uses a common **1.435 m standard gameplay gauge**, with wheel contact positions matched to the rails. The real AZ8A's gauge differs; this is an explicit common-gauge adaptation. Vehicles have metre-based dimensions, including the 20.32 m ACS-inspired engine and a 32 m two-section AZ-inspired engine. Curve radius and maximum speeds are compressed for Minecraft play.

Craft these track items (search their names in NEI):

| Item | Geometry |
|---|---|
| Straight Track | 16 m |
| Left Curve | 90 degrees, 16 m radius |
| Right Curve | 90 degrees, 16 m radius |
| Rising Track | 16 m horizontal run, 1 m rise |
| Railway Switch | straight route and right-hand curved route |
| Station / Stop Track | 16 m; a powered controller holds trains near its center |

Stand facing the intended forward direction and right-click the top of the ground with the track item. The clicked position is the **start**, not the center. The next segment starts exactly 16 blocks along a straight route. A right curve ends 16 blocks forward and 16 to the right; turn 90 degrees before placing the next segment. A rising segment ends 16 blocks forward and 1 block higher. The reverse journey descends that same slope.

Clear a route at least 3 blocks wide and 5 blocks high, and keep its placement footprint loaded. Build supporting ground or a bridge under the whole route. Tracks have an anchor block at their start, with a cached mesh covering the full segment. Break the anchor to remove a segment. Only the anchor is a normal block hitbox; the rest of the track surface is rendered rail geometry. Large vehicles use the actual track centerline rather than vanilla minecart movement.

Right-click a switch anchor, or apply redstone to it, to change routes. Occupied switch segments stay locked. Station track obeys redstone: power its anchor to request a stop; remove power and apply throttle to depart. Track ends, obstructions or inaccessible chunks cause a safe stop. Look ahead and brake before obstacles; there is no derailment or crash-damage simulation.

## Place and drive a locomotive

Lay enough connected track behind and ahead of the vehicle. Use a locomotive item on the route near its centerline. The entire vehicle needs clear, loaded track. Placement assigns ownership to the placing player. The owner, or a creative-mode player, can open controls and pack up the vehicle.

* **Right-click an engine:** take the driver seat.
* **Sneak + right-click:** inventory and controls.
* **Drive button:** enter from its control screen.
* **W / Up:** increase throttle and release brakes.
* **S / Down:** reduce throttle; once it reaches zero, apply brakes.
* **Space:** emergency brake and zero throttle.
* **H:** horn or steam whistle, with a cooldown.
* **L:** headlights.
* **R:** train controls while driving.
* **F5:** vanilla first/third-person camera selection.
* **Shift:** dismount.

Up/Down/H/L/R have configurable Minecraft key bindings under **Industrial Railway**. W/S/Space additionally follow the player's existing movement bindings while mounted. The train always follows rails; A/D do not steer it off the track. Throttle/brake buttons require the driver seat. First-person rendering uses a modeled cab with open glass areas, dashboard, gauges and controls; the opaque exterior shell is omitted for its driver. Other players see the exterior.

The control GUI shows speed in km/h, throttle, brake, energy or boiler/water state, attached wagon count, total mass and status. Movement is authoritative on the server and interpolated by clients. Full braking cuts traction. Trains accelerate gradually; heavier trains accelerate more slowly. Resistance, grades, speed and traction work affect consumption. A bounded look-ahead reduces speed for curves and track ends. Reverse movement as a driver control is not included: place a return curve or turn the route around for the opposite trip.

## Steam locomotive

The modeled engine includes a cylindrical boiler, chimney, domes, cab, cylinders, tender, wheels and moving side rods. Wheels and rods respond to actual travel; smoke and steam are bounded and depend on heat/motion.

Sneak-right-click to open its screen. Shift-click coal from your inventory, or put an ordinary stack on the cursor and click the coal bin. Its **one logical fuel bin holds 300 coal**. Charcoal is also supported unless disabled in config. Minecraft coal still stacks to 64 everywhere else.

Hold a **water bucket on the cursor** and press Water. Each bucket supplies 1,000 mB and returns an empty bucket. Capacity is 30,000 mB. Apply throttle to light the fire. Keep the brakes applied during warm-up if desired. Burning coal raises boiler temperature gradually; pressure becomes useful above 100 C. After several seconds, release the brakes and apply throttle. Moving under steam consumes water; coal and stored heat are finite. No water means no traction. After the last coal burns, residual heat decays rather than generating free perpetual steam. There is no boiler-explosion mechanic.

## Three electric engines

* **Industrial RF Electric:** freight-oriented, 5,000,000 RF onboard.
* **ACS-64 Passenger Electric:** original American-inspired livery, smoother passenger profile and higher speed limit; compatible with the passenger coach.
* **AZ8A Two-section Freight Electric:** two visible sections, red/white/gray original livery, roof equipment and pantographs, strong heavy freight traction, 10,000,000 RF onboard by default.

Electric traction consumes real RF according to force and distance; insufficient RF prevents further powered acceleration. Coasting and gravity remain physical effects. Stationary engines have no traction drain. RF has no direct equivalence to HBM HE.

Place an **RF Train Charging Station** within seven blocks of the stop area. Connect a standard legacy RF source or cable to it. It buffers 2,000,000 RF, accepts at most 50,000 RF/t across all faces, and charges stopped electric engines at at most 50,000 RF/t. Multiple stations cannot bypass an engine's own aggregate input limit. Its screen shows buffer RF, the detected locomotive's actual RF/capacity and percentage, and charging status. Rates and onboard capacity are configurable. Advanced Capacitor Banks and compatible CoFH/Thermal RF networks work through the real RF API; HBM requires a genuine HE-to-RF converter. Roof pantographs are folded equipment: this release supplies power through stations and does not include a catenary network.

## Freight and coupling

A freight vehicle is a steel flatbed with a ribbed shipping container, modeled doors/corner fittings, bogies and couplers. Right-click to open it. There are **exactly three logical cargo bins**:

* ordinary items: 100 per bin;
* coal: 300 per bin;
* charcoal: 300 if enabled.

Each bin holds one compatible item/metadata/NBT identity. Templates exceeding 8 KiB of encoded structural data, 512 NBT nodes or 12 nested levels are rejected before insertion. Counts are separate integers; fake oversized ItemStacks are never sent to ordinary inventories. Left-click a bin with an item on the cursor to insert; with an empty cursor to extract a normal stack. Right-click moves one item. Shift-click a player stack to insert; Shift-click a bin to extract into the player inventory. Extraction stops at the normal item's stack limit. Hover for the item name and controls.

Cargo adds mass. Defaults are 100 kg/item, coal 80 kg, iron ingots 180 kg, gold ingots 200 kg. Change `itemMassOverrides` using exact registry names, e.g. `minecraft:coal=80`. No mass is inferred from loose item-name matching. These are deliberately compressed bulk-transport approximations, not physical individual ingot masses.

Use the **Railway Coupling Tool**:

1. Stop both vehicles on the same connected route, facing the same direction.
2. Place the wagon so its front coupler is within roughly two metres of the preceding vehicle's rear coupler. Vehicle-center spacing is half the two lengths plus 0.8 m.
3. Right-click the engine with the tool to select its rear coupler, then right-click the first wagon.
4. Select that wagon's rear coupler, then the next wagon. Repeat for the rest of the consist.
5. Sneak-right-click a vehicle with the tool to uncouple its loaded rear neighbor.

You must own both vehicles. Cycles, a second powered engine behind the leader, conflicting couplings and excessive wagon counts are rejected. Default maximum is 16 wagons. Each vehicle integrates its own rail cursor by the same traveled distance, retaining its position and spacing through curves. UUID links persist across saves; an engine waits if a linked wagon is unloaded. Bogies follow local rail tangents, bodies follow the bogie chord, and the AZ sections articulate separately. This is a rigid-distance simplified consist, without elastic slack or a full coupler-force simulation.

## Quarry-to-factory automation

Place a **Rail Cargo Loader** within seven blocks of the stopped freight wagon. Supply its nine ordinary buffer slots through a compatible item pipe, or an adjacent chest/inventory. It pulls from adjacent inventories and loads logical bins at a configurable maximum (default 32 items/tick). Metadata, NBT and bin capacities remain authoritative.

At the destination, use a **Rail Cargo Unloader**. It extracts normal stacks into its nine-slot buffer, then pushes to adjacent inventories or lets pipes extract. Its GUI reports the detected vehicle, transfer count, status and available logical capacity. Both machines work while their chunks are loaded, without a nearby player. They do not force chunks themselves. Adjacent HBM inventories work if they implement ordinary/sided Forge-era IInventory; specialized machine ports must permit their normal insertion/extraction rules.

Example: Quarry -> chest/pipe -> Rail Cargo Loader -> stopped freight wagon -> drive -> Rail Cargo Unloader -> factory chest/pipe.

## Chunk loading, saves and servers

Active engines request an entity-bound Forge ticket, with at most nine chunks by default. The current consist takes priority, then the immediate neighboring area. A long consist that exceeds the permitted region stops. Configure a larger maximum, up to 25, only if Forge permits it. Tickets are released when the engine stops with no throttle. Trains stop before moving into unloaded track; they do not force an entire route or silently bypass ticket limits.

Parked vehicles, cargo, coal, water, stored RF, owner, headlights and couplings persist. Engines restore parked with zero throttle and brakes applied. Sneak-hit a stopped, unoccupied, uncoupled owned vehicle to pack it into an item; its cargo, fuel, water, heat and RF remain on that item. Track position and old couplings are removed from packed items.

## Scope and interoperability

Railcraft, Traincraft and Immersive Railroading APIs/releases were inspected before choosing a standalone native engine. Their code and rolling-stock models are not copied, and they are not required. This release's large trains use its own standard-gauge segments; they do not drive on vanilla, Railcraft, Traincraft or Immersive Railroading track. These mods may coexist, but no cross-mod coupler/track adapter is claimed.

The existing quarries, void filters, ore mining, bank networks, chunk loader, wireless sticks/terminal, automation timers and HBM reactor integration remain. Existing registration order and network IDs are retained; expansion content is appended. Deleted space content is not restored. Development and verification use disposable project worlds; no installed player world is edited.

The audio assets are original synthesized approximations of horns, wheel clatter, steam, motor and brake sounds. They are distance-attenuated, motion/state-driven and rate-limited. They are not field recordings. Source meshes, material atlas and WAV masters are in development-assets/railway; the repeatable generator is tools/create_railway_assets.py.
