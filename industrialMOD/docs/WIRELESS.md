# Wireless monitoring and automation — 1.5.1

Use the same Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8 setup as the existing
mod. Replace the old mod JAR on both client and server. Existing quarry IDs,
saved output, filter entries, cursors, chunk loading, and 20,000-RF/t balance
are preserved. HBM, AE2, and CoFHCore are optional.

## Start a network

1. Craft a Wireless Terminal and open it by **left-clicking** while holding it.
   Right-click also opens it. Its left-click handler suppresses attacking and
   breaking blocks while equipped.
2. Keep that terminal in your inventory. **Normally right-click** the ground
   or a machine's desired face with a colored stick. All three colors place a
   real torch in the adjacent empty/replaceable block. Sneaking is optional.
   While holding a stick, placement takes priority over opening a machine.
3. Reopen the terminal and choose **Monitors**, **Controls**, or **Automation**.
   Select a device to rename, inspect, or configure it.

You can place any color without carrying a terminal. It remains an unlinked
torch until you carry your terminal and right-click it with that terminal (or
a colored stick). The original placer owns the torch. Placing with a terminal
already in inventory links it automatically. A full network leaves the torch
placed but unlinked; link it after freeing capacity.

Green and Blue can sit on ordinary ground, but ground has no inventory or tank,
so their monitor status is UNSUPPORTED until used on a readable machine. For
useful monitoring/automation, mount directly on the machine's input/output face.
Each torch needs a free adjacent block. Use a spare face if a pipe occupies it.

Each terminal item binds to its creator when first used. Separate terminals
create independent networks. When carrying several, open the intended terminal
before registering sticks. Without an active selection, the first owned
terminal in inventory is used. Other players cannot use a stolen bound terminal
or control endpoints from another network. Shared access is not implemented.

The default limit is 128 endpoints per terminal and eight independent networks
per owner. The configurable world-wide endpoint limit defaults to 8,192. These
limits bound save size and processing. Device UUIDs, ownership, names, faces,
settings, and rule latches are saved in world data; target identity tokens are
stored in this mod's own chunk-data namespace, without rewriting machine NBT. Physical torch orientation, placer, and
linked/unlinked state are saved in its own TileEntity.

## Green Monitoring Stick

Green registers a read-only view of the clicked TileEntity and face. It reads
`IInventory`, `ISidedInventory`, and Forge `IFluidHandler`. Sided inventories
show the slots exposed on that face; hover populated icons for slot numbers
and supported input/output permissions. Counts, occupied/available slots,
tank amounts/capacities, and fill percentages come from the real server APIs.
Item contents are paged 27 slots at a time; tank pages show four tanks, each
with its own bar and exact amount/percentage tooltip.

Green and Blue are physical torches, with the same floor/wall/ceiling mesh
as Yellow. Placement consumes one stick in survival; creative stacks are
unchanged. Breaking a torch drops its own color. Unlinking removes a loaded
physical torch and drops exactly one stick; unloaded torches do so on reload.
Replacing the target TileEntity disconnects its old registration rather than
silently monitoring the new machine.

Existing 1.4.0 virtual Green/Blue links remain usable and keep their old behavior.
Placing the matching color on that linked face upgrades it to a physical torch
while preserving its network, name, UUID, and automation rules. Old Yellow
relays and all existing machine saves remain compatible.

Monitoring cannot transfer items or fluids. Only your own inventory and the
rule's non-owning ghost icon are actual container slots. Base item/metadata
icons are sent without arbitrary machine item NBT; exact names and counts are
reported separately. Large custom NBT-dependent cosmetic icons may look generic.

## Yellow Redstone Control Stick

Yellow places a real torch-shaped relay in the empty block adjacent to the
clicked face. It never replaces the machine. It supports floor, wall, and
ceiling mounting. The inventory variants share the same small wooden-stem torch
silhouette, with green, yellow, or blue tips. Yellow has lit/dim world textures.

The terminal's compact switch sets output to **0 OFF** or **15 ON**. Weak power
reaches adjacent blocks; strong power is directed into the attached support.
Neighbor updates happen on state changes. A receiving machine must accept
vanilla redstone on the selected face; this does not add redstone support to
machines that only expose a different control API.

**Manual mode** stays ON until switched OFF. **Pulse mode** runs for a separately
configured 5–10 seconds, default five. Click the duration to cycle values, and
use Trigger pulse or the switch to start it. Timers use 100–200 server ticks;
seven seconds is 140 ticks at 20 TPS. Another trigger during an active pulse is
ignored, without extending it. Switching modes stops the existing output.

Pulse deadlines and settings survive saves. Unloading does not keep the relay
running in an unloaded chunk; its deadline is checked when loaded again. If
the deadline has passed, it supplies no phantom power on reload. Breaking the
relay disables its output and leaves a disconnected registration to unlink.
Unlinking removes the registration; a loaded orphan relay drops its stick,
and an unloaded one does so when its chunk later loads.

## Blue Automation Stick — SIMPLE

Blue watches the registered inventory face and supports eight independent
rules. Use the rule arrows to select a rule, then:

1. Choose the condition button.
2. Pick up an item from your inventory and left-click the ghost slot to copy
   its icon. Put the real stack back. Right-click clears the icon.
3. Enter the minimum quantity in the adjacent number field.
4. Click the target button and select from the paged list of linked Yellow
   Sticks in that dimension. No coordinate entry is required.
5. Enable the rule and press **Apply rule**.

Supported conditions are Contains item, Item amount reached, Inventory full,
Inventory empty, and Item missing. Item comparisons use exact item, metadata,
and NBT identity. The quantity range is 1–1,000,000,000. Full with a selected
ghost item checks whether more of that item can be inserted through the face.
Without a ghost item, an empty opaque inventory slot is conservatively counted
as available; insertion constraints hidden from `IInventory` cannot be inferred.

An enabled/newly edited rule starts armed from FALSE. If already true, it can
activate once on its next successful poll. A true condition latches and does
not repeatedly trigger. It must become false, then true, to fire again.
Saving an unchanged rule preserves its latch; restart also preserves it.
Unavailable/offline reads do not fabricate an empty inventory or reset latches.
Triggers are not queued for offline Yellow targets. A later fresh edge is needed.

Blue uses the selected Yellow Stick's own mode and timer. Manual targets stay
ON; pulse targets stop themselves. Multiple rules can target the same relay;
active pulses ignore overlapping requests. One inventory summary serves all
eight rules. Polling defaults to ten ticks, with a configurable round-robin
visit budget, so a large registry may update more slowly.

For the stone example, configure Yellow to Pulse / 7 seconds, then save an
enabled Blue rule: Contains item / Stone / Minimum 1 / that Yellow target.
Adding stone to the real chest triggers 140 ticks of redstone output. Keeping
the stone does not retrigger it. Remove it, let the next poll see false, then
add it again for another activation. This works without a terminal GUI or a
nearby player while the relevant chunks remain loaded.

## CODE Mode and the Automation Guide

Select a Blue Stick in Automation, click CODE, type commands, Save, then Enable.
CODE and SIMPLE retain separate rules; only the selected mode runs. Switching
modes never erases either program or its trigger memories. The editor occupies
the right panel and hides old rule buttons and inventory slots while coding.
It has real multiline editing, mouse selection, clipboard shortcuts, undo/redo,
scrolling, line numbers, syntax colors, and line-numbered errors.

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "Pump" 7s
END
```

Pump must be a linked Yellow Stick with that exact name. Duplicate names are
rejected; the control tooltip also gives an `id:UUID` reference. Conditions read
actual exposed inventories/tanks and support ITEM, FLUID, INVENTORY EMPTY/FULL,
and TANK EMPTY/FULL. Actions are PULSE (5–10 seconds), ON, and OFF. Invalid saves
keep the draft and previously working program. Save alone does not enable it.

Green tank tooltips now show the recognized Fluid ID and tank index. Matching
fluid amounts are summed across the exposed set. HBM names are adapter-qualified
as `hbm:` plus the actual registered name; unknown names are errors. Missing API
data is never zero. Removing a mod can invalidate a saved ID; code is checked
once after loading and disables with an error until corrected.

PULSE ignores an already active pulse. ON/OFF cancel a timer; ON stays on until
another accepted command changes it. Within a script poll the last rising
action for the same target wins; stable true rules do not oscillate. Offline
affected rules preserve their latch and pause. No chunks/dimensions are loaded.

Craft the ordinary leather Automation Guide with Book + Redstone + Paper
(shapeless), then right-click it. The Help button uses the same classic book
reference without closing the terminal or losing a draft. Book pages are
automatically wrapped/paginated using the actual Minecraft font. Example pages
have a Copy example button for pasting into the editor.

Read [AUTOMATION_CODES.md](../AUTOMATION_CODES.md) for the complete command
reference, twelve projects, controls, units, limits, and troubleshooting.

## Range, status, and compatibility

Terminals use no RF and do not force-load targets. Use the existing Chunk
Loader or another loader when unattended operation is needed. ONLINE, OFFLINE,
OTHER DIMENSION, DISCONNECTED, UNSUPPORTED, and DATA UNAVAILABLE are distinct
states. Current remote interaction is restricted to the same dimension.
Names and links remain available for management when a target is offline.

The optional HBM adapter targets the inspected **1.0.27_X5808** public APIs:
Arc Furnace inventories, Chemical Plant tanks, Fluid Tank, Big Ass Tank, and
supported multiblock proxies. It reads `FluidTank.getFill/getMaxFill` as the mB
units used by that version's own GUI, without conversion. The server sends a
common fluid ID and translation key; HBM's client-only localized-name method
is never called on a dedicated server. Controller lookup checks loaded chunks
and uses a guarded block-access view. The inspected X5808 inventory/fluid capability flags are respected, so a power-only port is not treated as a complete inventory. Unsupported/unresolved ports fail safely.

See [the verification record](TESTING.md) for performed tests and remaining
coverage. Other HBM forks, full assembled multiblocks, dynamic Chemical Factory
delegates, and a particular machine's response to vanilla redstone still need
modpack verification. Arc Furnace molten-material lists are not Forge fluid
tanks and are not presented as tank contents. Direct HBM HE power is unchanged.

Active pages synchronize at ten-tick intervals and only send changed data.
Controls are validated on the server against the held terminal, owner, window,
endpoint, dimension, action rate, and fixed packet/text bounds. Monitoring scans
at most 4,096 inventory slots and 64 tanks; unsupported oversized inventories
report DATA UNAVAILABLE. Ghost icons retain the existing 1-KiB NBT bound.

## Crafting

Recipes use a 3×3 grid; rows are written top to bottom. A space is empty.

| Item | Pattern | Ingredients |
| --- | --- | --- |
| Wireless Terminal | `IGI / RDR / IEI` | I iron, G gold, R redstone, D diamond, E ender pearl |
| Green Monitoring Stick | `" E " / " R " / " I "` | E emerald, R redstone, I iron |
| Yellow Redstone Control Stick | `" G " / " R " / " I "` | G gold, R redstone, I iron |
| Blue Automation Stick | `" L " / " R " / " I "` | L lapis lazuli, R redstone, I iron |

These are ordinary shaped recipes and appear in NEI without an AE2/HBM dependency.

## Repeat while true

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

Name the linked Yellow Stick `WATER`. This fires immediately when true, then every
twenty seconds while true. Use `20m` for minutes or `20h` for hours. False resets
the timer. Timers use server ticks; shutdown time does not count. Saves/restarts
preserve pending deadlines; overdue loaded rules run once, without catch-up bursts.
Choose an interval longer than the pulse to get a visible OFF gap.

## HBM reactors and local search (1.6.0)

See [REACTORS.md](REACTORS.md) for RBMK depletion and °C, ZIRNOX native fuel
positions and adjacent supply assignments, native RBMK manual-rod controls,
and independent search in each terminal tab. SIMPLE/CODE and EVERY remain available.
