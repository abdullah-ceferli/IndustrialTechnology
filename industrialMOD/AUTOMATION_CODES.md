# Automation Codes — Industrial Technology 1.6.3

<!-- Reference SHA256: fbe04887b7fe8df1045a2957a35c41226cebfb24da64282e6b4a9333e9ddee89 -->

This is the complete reference for the Wireless Terminal SIMPLE/CODE system.
The in-game Automation Guide and editor Help use the same reference data.

## Welcome to Automation

The Wireless Terminal lets you see and control your linked devices. Blue watches a machine. Yellow sends redstone. Green shows its contents. You can use SIMPLE buttons or write CODE.

## Link your devices

Carry your own Wireless Terminal. Normally right-click a machine face with Blue and a receiving machine face with Yellow. Each torch needs an empty adjacent block. Existing torches can be linked by right-clicking them with your terminal.

## Name your controls

Open Controls and rename the Yellow Stick to Pump, or another clear name. Code uses that exact name inside quotes. Names are case-sensitive. Two controls with the same name are an error, not a random choice.

## Open Code Mode

Open the terminal, choose Automation, select a Blue device, then CODE. Type your program, click Save, then Enable. SIMPLE rules stay saved. Only the selected mode runs.

## Your first program

Put Blue on a chest and name a Yellow control Pump. Save this program. Adding stone makes Pump send five seconds of redstone.

```text
WHEN ITEM "minecraft:stone" >= 1
THEN PULSE "Pump" 5s
END
```

## WHEN, THEN, END

WHEN checks something. THEN chooses an action. END finishes one rule. Put each on its own line. Blank lines and lines starting with # are allowed. Keywords can use upper or lower case.

## Reading amounts

>= means greater than or equal to: at least that amount. < means less than. Amounts are whole numbers from 0 through 1000000000000. Item IDs look like minecraft:stone.

## Understanding mB

mb means millibuckets. One normal bucket is 1000 mB. 200mb is one fifth of a bucket. Fluid data comes from real exposed tanks; the script never estimates a fill level.

## Which fluid name?

Hover a tank in Monitors to see its Fluid ID. Forge water is water. HBM IDs are shown as hbm: followed by its actual registry name. A common unqualified name also matches the same HBM name. Display names are not IDs.

## A rule fires once

Without EVERY, a rule fires once when false becomes true. Stable true does not repeat. Drop below the threshold and reach it again for another pulse. Add EVERY before THEN to repeat while true.

## Repeat while true

EVERY is optional. Put it between WHEN and THEN. The first true check triggers immediately; later true checks trigger once per interval. False stops and resets the timer. s means seconds, m minutes, h hours. Use whole positive amounts. Time uses server ticks at 20 TPS; stopped servers do not advance timers.

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

## More than one rule

Put up to eight WHEN/THEN/END blocks in one editor. Each keeps its own true/false memory and optional repeat timer. When several actions are due for one target in the same poll, the last block in text order wins.

## Pulses and manual power

PULSE lasts 5-10 seconds. ON stays on; OFF stops immediately. ON/OFF cancel a running timer. A pulse already running ignores another pulse. Across separate Blue devices, server processing order decides the latest accepted command.

## Saving and switching

An invalid Save keeps the draft and last working program. Save does not enable disabled code. Switching SIMPLE/CODE keeps both programs. Unchanged rules retain their edge and EVERY deadline. Disable stops checks; active pulses finish, and manual ON stays on until OFF.

## Unloaded machines

Devices must be loaded in the same dimension. Scripts do not load chunks. Missing data never becomes zero. Affected rules keep their edge and repeat deadline. On return, an overdue true EVERY runs once and schedules from now, without a catch-up burst.

## What this cannot do

Commands only read supported storage and control your linked Yellow Sticks. They cannot transfer items, create RF/HE, run Java, or control a machine that ignores vanilla redstone. Other HBM forks and unverified multiblocks may show FLUID DATA UNAVAILABLE.

## Search linked devices

Each terminal tab has a Search box and X to clear it. Type part of a device name; upper/lower case does not matter. The search includes machine names and coordinates, keeps your selected device, and remembers a separate query for each tab.

## HBM fuel and temperatures

Attach Green to an RBMK fuel element or a ZIRNOX inventory port. Its Reactor page shows actual fuel depletion and remaining percentages. RBMK core/skin and ZIRNOX reactor temperatures are separate Celsius readings. Empty rods show EMPTY. HBM X5808 has no separate C depletion statistic.

## ZIRNOX fuel views

One Green port link reads all 24 native fuel positions. Grid shows all seven staggered rows together on one screen; List has six pages. Click a position and hover for its full fuel name, condition and depletion. Offline ports never load chunks.

## Assign ZIRNOX fuel

Select a Blue ZIRNOX port in Automation, then Reactor. Click a core position. Click Inventory and pick up a real ZIRNOX rod; you return to the full grid. Click its ghost square at the right. Nothing is consumed. Open Inventory again to return the real item, then Fuel grid.

## Run a local fuel feeder

Choose Supply Auto or a direction next to the linked port. Put a real chest or supported inventory there. Apply saves all assignments. Enable starts the feeder. It inserts real matching fuel only into empty assigned positions, preserving fuel NBT. Wrong or occupied rods are never removed.

## ZIRNOX assignment controls

ON/OFF changes the selected assignment draft. Clear removes only its assignment. All+ and All- change all nonempty drafts. Click Apply to save. Enable/Pause acts on saved assignments. SIMPLE and CODE remain saved and available separately.

## Remote RBMK control rods

Link Blue to the RBMK console core and set its normal HBM reactor anchor. Choose Rods in Automation. The slider snaps to ten-percent insertion steps. Fully Insert is 100%; Withdraw is 0%. Click Apply rods to send the request. Native rod motion and power rules still apply.

## RBMK control limits

Only connected loaded manual rods in the console native 15 by 15 view are commanded. Automatic rods are excluded. MIXED means actual rods differ; the displayed current value is their average. Read confirmation for blocked rods or unloaded boundaries. Protection and ownership still apply.

## Code words

| Word | Meaning |
| --- | --- |
| `WHEN` | Start a condition. |
| `THEN` | Choose the action. |
| `END` | Finish the rule. |
| `ITEM` | Count stored items. |
| `FLUID` | Count stored liquid. |
| `INVENTORY` | Check accessible item slots. |
| `TANK` | Check exposed liquid tanks. |
| `EMPTY` | Contains nothing. |
| `FULL` | Has no known spare capacity. |
| `PULSE` | Send temporary redstone. |
| `ON` | Keep redstone at 15. |
| `OFF` | Set redstone to 0. |
| `>=` | At least this amount. |
| `<` | Less than this amount. |
| `mb` | Millibuckets: 1000 mB is a bucket. |
| `s` | Seconds: 5s is five seconds at 20 TPS. |
| `EVERY` | Repeat while true after the chosen interval. |
| `m` | Minutes: 20m is twenty minutes at 20 TPS. |
| `h` | Hours: 20h is twenty hours at 20 TPS. |

## Full command reference

### ITEM

Syntax:

```text
WHEN ITEM "item_id" >= amount
WHEN ITEM "item_id" < amount
```

Count a real item. >= means at least; < means fewer than.

Working example:

```text
WHEN ITEM "minecraft:stone" >= 1
THEN PULSE "Pump" 5s
END
```

Expected result: Adding one stone triggers once.

Limits: Use registered item IDs. CODE counts all metadata/NBT variants of that item; use SIMPLE ghost rules for exact variants.

### INVENTORY EMPTY

Syntax:

```text
WHEN INVENTORY EMPTY
```

Check whether all accessible inventory slots are empty.

Working example:

```text
WHEN INVENTORY EMPTY
THEN PULSE "Pump" 5s
END
```

Expected result: The empty chest triggers once.

Limits: A missing or unsupported inventory is unavailable, not empty.

### INVENTORY FULL

Syntax:

```text
WHEN INVENTORY FULL
```

Check whether accessible slots have no known spare stack space.

Working example:

```text
WHEN INVENTORY FULL
THEN PULSE "Pump" 5s
END
```

Expected result: A full chest triggers a storage warning once.

Limits: Empty opaque slots count as available. Hidden custom slot limits cannot be inferred. SIMPLE can test fullness for one ghost item.

### FLUID

Syntax:

```text
WHEN FLUID "fluid_id" >= amountmb
WHEN FLUID "fluid_id" < amountmb
```

Count actual millibuckets of the named fluid across the exposed tanks.

Working example:

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "Pump" 7s
END
```

Expected result: 100 mB is false. Reaching 200 mB triggers seven seconds of redstone.

Limits: Matching amounts are summed across the selected Forge face. HBM uses its public supported tank set; fluid-enabled proxies resolve the loaded controller. Hover a Green monitor tank to copy its ID. Unknown names are rejected; unavailable reads pause the rule.

### TANK EMPTY

Syntax:

```text
WHEN TANK EMPTY
```

Check whether every exposed supported tank contains no liquid.

Working example:

```text
WHEN TANK EMPTY
THEN PULSE "Pump" 5s
END
```

Expected result: The empty tank set triggers once.

Limits: All exposed tanks are considered, not only water. No supported tanks means unavailable.

### TANK FULL

Syntax:

```text
WHEN TANK FULL
```

Check whether every exposed positive-capacity tank has reached capacity.

Working example:

```text
WHEN TANK FULL
THEN PULSE "Pump" 5s
END
```

Expected result: A full tank set triggers once.

Limits: Every exposed tank must be full. Zero-capacity tanks do not count; a set with no positive capacity is unavailable.

### EVERY

Syntax:

```text
WHEN condition
EVERY 20s
THEN action
END
```

Repeat the action while the condition stays true. Use EVERY 5s, 10s, 20s, 20m or 20h. The first true check runs immediately, then the interval is measured from each due action. Use one EVERY line between WHEN and THEN.

Working example:

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

Expected result: WATER pulses for five seconds immediately at 200 mB or more, then again every twenty seconds while water stays at least 200 mB.

Limits: Whole positive s/m/h durations only, with overflow rejected. Pulse length remains 5-10s. An active pulse ignores another pulse, so choose an interval longer than the pulse to get a visible OFF gap. False resets the timer. Unavailable data/targets pause actions. Timers and unchanged saves survive restarts; overdue rules run once on resume, never a burst. Timings use server ticks, so lag may delay them.

### PULSE

Syntax:

```text
THEN PULSE "Control Name" 5s
THEN PULSE "Control Name"
```

Send redstone power 15 for five through ten seconds, then return to zero.

Working example:

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "Pump" 7s
END
```

Expected result: Pump turns on for 140 server ticks at 20 TPS.

Limits: Without a duration, use the Yellow Stick's configured duration. An already active timed pulse ignores another pulse and keeps its original deadline. This command works even if the target is configured Manual.

### ON

Syntax:

```text
THEN ON "Control Name"
```

Set redstone output to 15 until an OFF command or a later pulse changes it.

Working example:

```text
WHEN ITEM "minecraft:iron_ingot" >= 64
THEN ON "Machine Power"
END
```

Expected result: Machine Power stays on.

Limits: ON cancels an existing pulse timer. It does not change the target's saved mode/duration settings.

### OFF

Syntax:

```text
THEN OFF "Control Name"
```

Set redstone output to zero now.

Working example:

```text
WHEN ITEM "minecraft:coal" < 16
THEN OFF "Machine Power"
END
```

Expected result: Machine Power turns off and its pulse timer is canceled.

Limits: The receiving machine must accept vanilla redstone on that face.

## Example projects

Before using an example, give your Yellow Stick the quoted name in Controls.
For oil and other mod fluids, the named fluid must actually be installed.

### 1. Stone starter

```text
WHEN ITEM "minecraft:stone" >= 1
THEN PULSE "Stone Processor" 5s
END
```

Add stone to the attached chest. Stone Processor pulses once.

### 2. Stack of iron

```text
WHEN ITEM "minecraft:iron_ingot" >= 64
THEN PULSE "Factory Machine" 10s
END
```

Store at least 64 iron ingots. Factory Machine pulses for ten seconds.

### 3. Low coal warning

```text
WHEN ITEM "minecraft:coal" < 16
THEN PULSE "Coal Warning" 7s
END
```

Fewer than 16 coal triggers the warning. Refill before another low edge.

### 4. Empty chest

```text
WHEN INVENTORY EMPTY
THEN PULSE "Refill System" 5s
END
```

Remove the last item from the chest to trigger Refill System.

### 5. Full chest

```text
WHEN INVENTORY FULL
THEN PULSE "Storage Alarm" 5s
END
```

Fill all accessible slots to trigger Storage Alarm.

### 6. Water at 200 mB

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "Arc Furnace" 7s
END
```

Start below 200 mB, then add water. Arc Furnace pulses once for seven seconds.

### 7. Oil at 1000 mB

```text
WHEN FLUID "oil" >= 1000mb
THEN PULSE "Oil Pump" 5s
END
```

Needs an installed fluid registered as oil. Hover its tank for the actual ID if different.

### 8. Empty tank

```text
WHEN TANK EMPTY
THEN PULSE "Empty Tank Alarm" 5s
END
```

An exposed empty tank set triggers the alarm.

### 9. Turn power on

```text
WHEN ITEM "minecraft:stone" >= 64
THEN ON "Machine Power"
END
```

At 64 stone, redstone stays on.

### 10. Turn power off

```text
WHEN ITEM "minecraft:stone" < 16
THEN OFF "Machine Power"
END
```

Below 16 stone, redstone switches off.

### 11. Refill water

```text
WHEN FLUID "water" < 500mb
THEN PULSE "Water Refill" 10s
END
```

Low water starts Water Refill for ten seconds.

### 12. Full tank alarm

```text
WHEN TANK FULL
THEN PULSE "Tank Full Alarm" 5s
END
```

All exposed tanks reaching capacity starts the alarm.

### 13. Repeat water every 20 seconds

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

Name your Yellow Stick WATER. At 200 mB or more, trigger immediately and every twenty seconds while true. Below 200 mB stops repeats.

### 14. Repeat water every 20 minutes

```text
WHEN FLUID "water" >= 200mb
EVERY 20m
THEN PULSE "WATER" 5s
END
```

Name your Yellow Stick WATER. At 200 mB or more, trigger immediately and every twenty minutes while true. Below 200 mB stops repeats.

### 15. Repeat water every 20 hours

```text
WHEN FLUID "water" >= 200mb
EVERY 20h
THEN PULSE "WATER" 5s
END
```

Name your Yellow Stick WATER. At 200 mB or more, trigger immediately and every twenty hours while true. Below 200 mB stops repeats.

## Combining rules

```text
WHEN ITEM "minecraft:stone" >= 1
THEN PULSE "Stone Processor" 5s
END

WHEN ITEM "minecraft:iron_ingot" >= 64
THEN PULSE "Factory Machine" 10s
END

WHEN ITEM "minecraft:coal" < 16
THEN PULSE "Coal Warning" 7s
END
```

Each block runs independently, up to eight. Only the selected SIMPLE or CODE mode runs.
A pulse can replace manual ON; it uses its own deadline without changing the saved mode.
A later ON or OFF cancels the timer. An active pulse ignores another pulse.
Within one poll, the last due block for the same target wins. Stable true conditions
without EVERY do not repeatedly fire. Different Blue devices use deterministic visit order.
EVERY repeats from each due check while true. False resets it; unavailable targets keep
the saved deadline. Overdue resume runs once and schedules from now, without bursts.
Unchanged saves and restarts preserve timers. Shutdown time does not advance game ticks.

New or edited rules start unlatched. Existing unchanged rules, chunk reloads, and server
restarts preserve latches. Offline/unknown affected rules keep their previous latch.
A condition that first becomes true while its target is offline can fire once on resume;
a previously latched true condition does not fire again merely because the chunk reloads.

Controls are compiled to authorized UUIDs. Renaming a control keeps a saved program working;
update its quoted name before saving the program again. For ambiguous names use
`"id:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"` from the Controls tooltip.

Keywords ignore case; quoted IDs/names are exact except unqualified HBM fluid name matching.
Escape a quote inside a name as `\"` and a backslash as `\\`.

The default script limit is 4096 characters, configurable from 256 to 8192. Uploads have
an additional 24576-byte UTF-8 bound. At most eight finite rules run; no loops or OS calls.
Polling defaults to ten server ticks; large registries and low TPS can update more slowly.

## Editor keys

Enter: new line. Backspace/Delete: remove text or the selection. Arrow keys: move the cursor.
Shift + arrows: select. Home/End and Page Up/Down: move around. Ctrl+A: select all.
Ctrl+C/X/V: copy, cut, paste. Ctrl+Z/Y: undo/redo. Mouse: place cursor or drag a selection.
Mouse wheel: scroll. Long lines scroll horizontally to keep the cursor visible.
Code input captures normal typing so movement/hotbar keys do not act on the game.
Help opens the ordinary book reference and preserves your editor draft.

## Craft the Automation Guide

Shapeless recipe: one ordinary Book + one Redstone + one Paper = one Automation Guide.
Right-click the brown leather book to read. Use the page arrows; Done closes it.
It uses the classic Minecraft book page texture, with cream pages and dark text.

## Troubleshooting

### Unknown item

Check the full registered ID, such as minecraft:iron_ingot. CODE item checks count all variants of that item. If removing a mod makes a saved ID unknown, the program disables until you fix and save it.

### Unknown fluid

Copy the Fluid ID shown when hovering a tank in Monitors. A fluid that is not installed cannot be used. Unknown fluid is a Save error, not zero mB. If removing a mod makes a saved ID unknown, the program disables until you fix and save it.

### Unknown control

Rename a Yellow Stick in Controls, then use its exact name inside quotes. If names are duplicated, rename one or use "id:UUID" from the control tooltip. Controls must belong to this terminal and dimension.

### Invalid command

Use only documented WHEN conditions and THEN PULSE/ON/OFF actions. Check quotes, whole numbers, mb, and s. The error tells you which line to fix.

### Missing END

Add END on its own line after each THEN action.

### Target offline

Load the machine and Yellow Stick chunks with your existing chunk loader if needed. Scripts wait and never force-load them.

### Unsupported machine

Mount Blue directly on a readable inventory or tank face. Plain ground has no storage. Unknown APIs report unavailable instead of triggering low-resource rules.

### No permission

Use your own bound terminal and linked sticks. A stolen terminal or a control from another network is rejected.

### Repeat not firing

Place EVERY 20s, 20m or 20h on its own line between WHEN and THEN. Save and enable CODE. Keep the tank and Yellow control loaded. Each active pulse ignores another pulse; use a longer interval than pulse duration for an OFF gap. Server shutdown time does not count.

## Machine compatibility

Forge IInventory/ISidedInventory and IFluidHandler are supported. The clicked face defines
accessible slots/tanks. Monitoring is read-only; no fill/drain/transfer calls are made.
For multiple tanks, named-fluid quantities are summed. EMPTY/FULL consider the complete
exposed set. Oversized, broken, or unresolved APIs pause rules instead of returning zero.

The optional HBM adapter targets inspected 1.0.27_X5808 public APIs: Chemical Plant, Fluid Tank,
Big Ass Tank and supported fluid-enabled multiblock proxies. Its public getAllTanks set is
identified as such; only API-exposed tanks are included. Arc Furnace molten-material lists
are not these fluid tanks. Other assembled structures and dynamic Chemical Factory delegates,
other HBM forks, and individual machines' redstone behavior require modpack verification.
No direct HE conversion or general machine-control API is added.
See docs/TESTING.md for the actual tested release and remaining coverage.
