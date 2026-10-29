# Bulk Steel Factory — 2.5.1

Find **Bulk Steel Factory** in the Industrial Technology creative tab or NEI. Right-click it to open its inventory. HBM Nuclear Tech is required to operate it; the rest of Industrial Technology still loads without HBM.

| Per batch | Amount |
| --- | ---: |
| Iron ingots | 16 |
| Coal dust | 4 |
| Genuine HBM steel ingots | 16 |
| Processing time at 20 TPS | 5 seconds (100 ticks) |
| Power while working | 500 HE/t |
| Total energy per batch | 50,000 HE |
| Internal HE buffer | 1,000,000 HE |

The central upper input accepts coal dust; the central lower input accepts iron ingots. Finished steel appears in the large output slot on the right. There are no separate input or output buffers. The coal input, iron input and steel output each hold one normal stack. Any stored items from an older version automatically drain into these visible slots as space becomes available. HBM coal powder and Thermal Foundation pulverized coal work; other mods' normal `dustCoal` entries also work. Ordinary coal items and charcoal are not dust. The output is HBM's own `ingot_steel`, so it is directly usable in HBM crafting recipes.

Connect an **HBM red copper power cable** to any face of the factory. The factory accepts real HBM HE and does not take RF. The Advanced Capacitor Bank continues to store RF separately. It draws no processing power while missing materials or when the steel output is full. A power shortage or blocked output pauses unfinished work. Its HE receiver accepts up to 20,000 HE/t so all supported upgrade combinations can receive sufficient cable power. Server saves preserve energy, inventory, and paid processing progress. Removing necessary input materials cancels the current progress. Breaking the machine drops its buffered items as ordinary stacks; the block item does not retain its HE charge or progress.

## HBM upgrades — two items maximum

The two slots on the right each hold **one HBM upgrade item**. Speed, Power Saving, Effectiveness and Overdrive tiers I–III work. Two items can be the same type or different types; combined levels of each type are capped at III. A third upgrade cannot be inserted. Upgrades are manually installed and removed; ingredient pipes and import buses cannot take them.

| Upgrade | Effect per effective level |
| --- | --- |
| Speed | Subtract 25 ticks from base time; add 500 HE/t |
| Power Saving | Add 10 ticks to base time; subtract 150 HE/t |
| Effectiveness | Save 1 coal dust per batch; add 125 HE/t |
| Overdrive | Divide processing time by `(1 + level)`; multiply HE/t by `(1 + 2 × level)` |

Effects combine before Overdrive's multiplier. Time rounds up to a whole tick; iron input and steel output always remain 16 per batch. Examples for one installed tier III item:

| Installed upgrade | Time | HE/t | Coal dust | HE/batch |
| --- | ---: | ---: | ---: | ---: |
| None | 5.00 seconds | 500 | 4 | 50,000 |
| Speed III | 1.25 seconds | 2,000 | 4 | 50,000 |
| Power Saving III | 6.50 seconds | 50 | 4 | 6,500 |
| Effectiveness III | 5.00 seconds | 875 | 1 | 87,500 |
| Overdrive III | 1.25 seconds | 3,500 | 4 | 87,500 |

The screen shows the current batch's time and HE/t; hover the diagram for its ingredient amounts and total HE, the energy bar for the stored HE, or an upgrade for its machine-specific effects. Upgrades take effect on the **next batch**. A started batch keeps its original duration, HE/t and coal requirement, including through pause, save and restart. This prevents swapping upgrades partway through to bypass costs. Fortune, laser-miner upgrades and other unrelated HBM upgrades are rejected.

Old 2.5.0 machines keep their HE, partial progress and every stored item; their two new upgrade slots start empty. Retired buffer items automatically move into the visible slots and can be used or extracted normally. New machines only accept materials in their three visible slots. A partial old recipe finishes with its original 100 ticks / 500 HE/t / 4 coal requirement.

## NEI recipe lookup

Hover **HBM steel ingots** in NEI and press **R**, then select **Bulk Steel Factory** with the recipe-type arrows. Its recipe appears alongside HBM's existing recipes: **16 iron + 4 coal dust → 16 HBM steel**, **5 seconds**, **500 HE/t**, **50,000 HE per batch**. This is the base recipe; installed upgrades change the operating values shown in the factory screen. Press **U** over iron ingots or coal dust to find the factory as an item usage too. NEI remains optional.

## Survival crafting

First craft a **Steel Factory Core**:

```text
Steel block | Steel block   | Steel block
Steel block | Diamond block | Steel block
Steel block | Steel block   | Steel block
```

Then craft the factory:

```text
Tungsten block      | HBM electric motor | Tungsten block
Industrial circuit | Steel Factory Core | Industrial circuit
Tungsten block      | HBM electric motor | Tungsten block
```

The total cost includes 72 steel ingots, 36 tungsten ingots, a block of diamonds, two HBM electric motors, and two Industrial Technology circuits. Steel and tungsten blocks use the normal ore dictionary entries. The expensive construction cost buys bulk production with straightforward inputs and moderate operating consumption. Both recipes appear in NEI.

## Thermal Dynamics / Thermal Expansion

1. Put iron ingots and coal dust in a chest.
2. Connect the chest to the factory with itemducts. Attach a **servo to the chest end** of the itemduct route and set its redstone control to **Ignored / Disabled** for continuous operation.
3. Connect a second itemduct route from the factory to an output chest. Attach a **servo to the factory end** of this route and ignore redstone there too.
4. Feed HE through HBM power cables. The itemducts transport items only.

All six factory faces accept ingredients and allow extraction of finished steel. Servos cannot pull the iron or coal dust back out. Output slots reject pipe insertion. There is no side configuration to set up. Normal itemduct routing and destination space are still required.

## Applied Energistics 2

Use an **ME Export Bus** to supply iron and coal dust and an **ME Import Bus** to collect steel. For one export bus carrying both ingredients, install an AE2 Capacity Card and configure both filter entries, or use separate export buses. Keep the ME network powered and connected. An ME Interface can also insert an exact processing pattern of `16 iron + 4 coal dust -> 16 HBM steel`; put the return import bus on another available face. The factory uses ordinary sided inventory rules, including simulated insertion, so no proprietary machine adapter is required.

## Screen, appearance and capacitor colors

The compact HBM-style screen has coal above iron, correctly aligned flames, a large steel output, an HBM electrical bar at the far right and two upgrade slots below it with small gaps. There is no lava gauge, buffer section or connector line beside the upgrade slots. The cooking arrow begins empty and fills left to right inside its outline as the current batch progresses.

The factory uses the installed HBM **Nuclear Blast Furnace (LEGACY)** textures directly: original side casing, active/inactive front, and active/inactive top. HBM assets are resolved from HBM's own texture atlas; no HBM PNGs, models, classes, or APIs are copied into this mod. Active appearance follows actual processing and returns to idle when blocked or unpowered. This appearance does not add a nuclear fuel requirement.

Advanced Capacitor Bank input faces now show a **green +**, and output faces show a **red −**. Its GUI uses matching input/output colors. The charge bar and stored-RF figure turn **gold at exactly full capacity**. An optional world gauge turns gold at full charge too; a nearly full bank stays in its regular gauge color. Sneak-right-click a Both face with an empty hand to enable its gauge, as before. Empty banks still stack to 64 and charged banks stay separate.

Tested combination: Forge 10.13.4.1614 / Java 8 / HBM X5771 / AE2 rv3-beta-6 / Thermal Dynamics 1.2.1-172 / Thermal Foundation 1.2.6-118 / Thermal Expansion 4.1.5-248. The HE adapter targets the installed X5771 API; compatibility with other HBM API revisions has not been established.
