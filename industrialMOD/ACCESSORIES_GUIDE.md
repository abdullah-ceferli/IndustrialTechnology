# Standalone accessories and ProjectE tools — 2.10.0

Five independent items are registered under **Industrial Technology**:

| Item | Use |
| --- | --- |
| Ring of Flight | Wear in either Baubles ring slot for creative-style flight. Double-tap jump to fly. No fuel. |
| Amulet of Nightvision | Wear in the Baubles amulet slot for continuous night vision. No fuel. |
| Destruction Catalyst | Right-click a block to mine a 3×3 tunnel, with depth determined by charge. |
| Hyperkinetic Lens | Right-click or press R to fire an explosive lens projectile. |
| Catalytic Lens | Right-click for deeper 3×3 mining; R fires a lens projectile. |

The screenshot's three ProjectE items were identified by exact original icon colors as Destruction Catalyst, Hyperkinetic Lens and Catalytic Lens, in that order. These are not the Life/Soul/Body stones.

## Independence and equipment screen

**ProjectE and Baublelicious are optional.** Their installed JARs are left untouched by this update. The new items, recipes, icons, sounds, projectiles, loot collection and fuel code remain functional with both removed. Their original items retain their own IDs; no existing items or inventories are replaced or migrated.

**Keep the original Baubles mod installed.** It supplies the standard four equipment slots, gold-ring inventory button and equipment persistence/networking. Open your normal inventory and click its small gold ring, or use Baubles' inventory key (default B). Put the Flight Ring in either ring slot and the Nightvision Amulet in the amulet slot. Empty-slot right-click equipping is supported too. Flight is restored while the ring is worn after joining; removing the final ring stops Survival flight, while Creative flight and a second worn flight ring are retained.

Find the new items in the **Industrial Technology** creative tab or NEI. Hover over an item to check that it belongs to Industrial Technology when both original mods are present. Each item is unstackable. This update adds no online service or AI feature.

## Controls and coal fuel

- **V:** increase held tool charge. **Sneak + V:** decrease it.
- **Right-click:** mine with either catalyst, or fire with Hyperkinetic Lens.
- **R:** fire Hyperkinetic or Catalytic Lens. Destruction Catalyst has no projectile mode.

When ProjectE is present, these controls share its existing charge/projectile key bindings; when absent, our own V/R bindings appear under Industrial Technology in Controls. Only the sender's current held tool can receive the command. Existing unrelated ProjectE tools keep their own controls.

**Only coal families fuel these tools:** coal, charcoal, coal blocks, supported coal/coke ore entries, and HBM coal-family items including Coal Coke, Lignite Coke and Petroleum Coke. Fuel can be anywhere in the player inventory. Wood/logs/planks/sticks/saplings, lava, blaze items, redstone and other ordinary furnace fuels do not qualify. No ProjectE colored fuels, matter items, trees or alchemy chain are added.

The original EMC cost tables are retained, with coal-only conversion. One vanilla coal supplies 128 credit, charcoal supplies 32, and a coal block supplies 1,152. Recognized mod coal/coke uses its real furnace burn value relative to coal, with bounded credit. Unused paid fuel remains in the tool's saved `StoredEMC` tag. Insufficient fuel consumes nothing; Creative use requires no fuel.

Mining costs 8 credit per eligible attempted block. Destruction Catalyst charge 0–3 gives depths **1, 4, 8, 16**. Catalytic Lens charge 0–7 gives **1, 16, 24, 32, 40, 48, 56, 64**. Both mine 3×3 cross-sections, skip hardness ≥50/unbreakable blocks and use original-style silk/fortune drops. Claims/break events and unloaded chunks are respected.

Projectiles cost **384, 768, 1,536, 2,304** credit across the first four charges (2,304 at higher Catalytic charges); their blast radii are **4, 8, 12, 16**, capped at 16. They follow the original zero-gravity projectile, water extinguishing, lifetime, ray/resistance-based Nova blast and collected-drop behavior. Nova blasts do not deal entity damage. Dropped loot balls persist their contents and support partial inventory pickups.

## Survival recipes

All recipes are visible in NEI. The two Baublelicious accessories preserve their original grids and vanilla ingredient chains:

- Plain Ring: gold nuggets at top/bottom center, iron ingots left/right center.
- Plain Amulet: leather at top center and left/right center, iron ingot bottom center.
- Tier 2 Magic Core: diamonds top/bottom center, redstone left/right, gold block center.
- Tier 3 Magic Core: gold ingots top/bottom, redstone left/right, Nether Star center.
- Ring of Flight: Tier 3 core above a plain ring, feathers to its sides, diamond below.
- Nightvision Amulet: Tier 2 core above a plain amulet, golden carrots to its sides, an ordinary 3-minute Night Vision potion (8198) below.

The three pictured tools have the user's requested **alternative recipes**, with no ProjectE matter or special coal crafting prerequisites:

| Tool | Crafting grid |
| --- | --- |
| Destruction Catalyst | `GOG / RDR / GOG`: G glowstone dust, O obsidian, R redstone block, D diamond. |
| Hyperkinetic Lens | `GQG / DED / GQG`: G glass, Q quartz, D diamond, E Ender Pearl. |
| Catalytic Lens | `DND / HRC / DND`: D diamond, N Nether Star, H our Hyperkinetic Lens, R redstone block, C our Destruction Catalyst. |

As with the original crafting grids, ingredients' existing charge/fuel is consumed when combined into the newly crafted tool. New tools begin uncharged. Original textures and relevant audio are copied byte for byte from the user's installed original mod versions; provenance/hashes are recorded in `development-assets/accessories/original-assets.json`.

## Scope

These are independent versions of the five requested items, with original primary actions and the explicitly requested recipe/fuel changes. This update does not import ProjectE's transmutation system, Klein Stars, alchemy fuels, collectors or its other items. Its separate optional bag/Black Hole Band integrations are not dependencies of our loot balls. Baublelicious's other accessories and machines are not imported. Existing quarries, wireless devices, reactors, banks and the full bundled OpenComputers content remain in the mod.
