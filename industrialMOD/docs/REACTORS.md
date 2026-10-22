# HBM reactors and terminal search — 1.6.3

Optional integration targets the installed **HBM-NTM 1.0.27_X5808** on
Minecraft 1.7.10 / Forge 10.13.4.1614 / Java 8. HBM is not bundled or required
for normal quarry, chunk loader or wireless use. Other HBM forks need validation.

## Green fuel monitors

Carry your terminal and place Green on the RBMK fuel element/core or its native
inventory-enabled top access cap. Select it in Monitors. Its reactor panel shows
the actual fuel item/name, state, depletion, remaining fuel, rod core temperature
and rod skin temperature in **°C**. Temperature and depletion are separate values.
No separate C depletion field exists in X5808; the displayed depletion follows
HBM's own yield/enrichment calculation. Missing/invalid readings display N/A.

For ZIRNOX, attach Green to an actual inventory-enabled input/output port or
controller. The associated controller's inventory supplies every native fuel
position: **24 positions in seven staggered rows, 3/4/3/4/3/4/3**. Grid shows
all seven rows on one screen with full-size item icons; List shows four positions per page. Click a position and
hover for the full fuel name, row/column, native slot, depletion and condition.
Reactor temperature is shown separately in °C; there is no per-rod ZIRNOX
temperature in the inspected implementation. The detail tooltip also shows
native reactor pressure in bar.

Names/icons come from installed items and metadata. Empty positions show EMPTY
without a stale icon or percentage. Reads use detached fuel copies because HBM
getters initialize absent NBT. Monitoring does not modify real fuel. Links remain
when a rod is removed. A replaced controller requires relinking; an unloaded
target shows OFFLINE and is never force-loaded. Coordinates are visible below
the panel and available in its coordinate tooltip.

## Blue ZIRNOX fuel assignments

1. Place Blue on a supported ZIRNOX inventory port/controller and select it in
   Automation. Open **Reactor** beside SIMPLE and CODE.
2. Click a native core position. Click **Inventory** beside Supply, then pick up
   the desired real ZIRNOX fuel item. The inventory drawer returns to the complete
   grid automatically. Click the ghost square at the right; it consumes nothing.
   Open Inventory again to return your cursor item, then click **Fuel grid**.
   Escape closes the drawer without closing the terminal. Right-click clears a ghost.
3. Choose the assignment ON/OFF. Repeat for other positions. Clear, All+ and All-
   edit the draft. **Apply** saves all assignments and the supply direction.
4. Put a real supply inventory immediately adjacent to the **linked access block**.
   Supply Auto checks Down, Up, North, South, West, East and prefers an allowed
   inventory with extractable fuel needed by an empty assigned position. An earlier
   empty or unrelated inventory no longer masks stocked supply. A specific direction
   always uses that side, without silently switching to another source.
5. Click **Enable** to run the saved feeder. Pause stops it without erasing rules.

X5808 has no separate fuel buffer: its automation-accessible 24 slots are the
core itself. This integration supplies those slots from the local inventory,
using the verified native insertion rules. It does not relocate occupied core
fuel or source items from a remote player's inventory. Desired assignments match
registered item + metadata. Real moved rods retain all NBT, including depletion;
the ghost does not demand an identical NBT/depletion state.

In Blue Grid, **bright rods are actually installed**. Dim rods with **P** are
only plans for empty positions. A red **!** marks wrong installed fuel. Hover a
position to see both **Installed** and **Assigned** fuel names. The header shows
RUNNING/PAUSED or APPLY PLAN for unsaved changes, the actual chosen supply, and
the installed rod count. Hover the header for its live source status and exact
chest/access coordinates; a missing source reports NO ADJACENT SUPPLY, and absent
required fuel reports NO MATCHING FUEL. Every configured type must actually be
stocked: natural uranium cannot fill a thorium assignment.

External pipes inserting directly into HBM are outside the feeder's slot selection.
Feed them into the adjacent supply chest and let Blue move rods into the core.
The chest must directly touch the linked access block, not just a distant duct or
another part of the multiblock. Existing wrong rods must first be removed through
the native reactor GUI. The feeder neither rewrites nor relocates occupied rods.

An occupied matching slot reports FILLED. A different rod reports WRONG FUEL and
stays untouched. Missing supply reports WAITING FOR FUEL and resumes on arrival.
Disabled/unassigned positions do nothing; inaccessible operations report a
blocked/unsupported/error status. The complete feeder feedback is in its tooltip.
Supply extraction and destination insertion restrictions are respected. Both
access point, controller and source must pass owner protection checks.

The feeder is independent of saved SIMPLE/CODE selection; either ordinary mode
can continue alongside enabled reactor assignments. No reactor DSL commands are
introduced. Existing WHEN/EVERY/THEN/END programs keep their behavior.

## Blue RBMK control rods

Place Blue directly on the native RBMK console **core** at an exposed face (its
underside is available on a raised console). The decorative multiblock surface
without a TileEntity is not a controller link. Configure the console's normal HBM
reactor anchor first. In Automation select the console link and open **Rods**.

The slider selects insertion from 0 to 100 in steps of 10. Fully Insert selects
100%; Withdraw selects 0%. **Apply rods** sends the request. These buttons first
change the draft so you can review before applying it. HBM internally calls the
opposite quantity extraction: native level 0 means fully inserted, 1 withdrawn.
The adapter converts insertion correctly and invokes native setTarget. Actual
level moves on native ticks, with normal power/speed rules; it is not overwritten.

The group includes loaded **manual** rods in the connected RBMK component rooted
at the configured anchor, clipped to the console's native rotated 15×15 view.
Disconnected nearby reactors are excluded. Automatic regulating rods remain
under HBM control and are counted separately. Actual differing rods display
MIXED plus their average insertion; differing native targets display MIXED.
Confirmation reports accepted/identified, blocked rods, automatic exclusions,
and unloaded boundaries. Incomplete loaded groups can produce partial success.
Nothing loads another chunk to find additional rods.

## Search

Monitors, Controls and Automation each have their own compact search box and X.
Matching ignores letter case and includes partial device names, machine names,
and coordinates. Queries filter locally over the owned catalog; typing does not
send a request or world scan. A query change returns to the first page, six
matching devices per page. Clear returns all devices in that tab. Selected
devices remain selected when filtered out; the old device-count caption is removed.
Renames refresh metadata through the normal update stream. Typing captures keys;
Escape/Enter leave search focus before other terminal actions.

## Data, bounds and compatibility

- RBMK depletion = (1 − native getEnrichment(copy)) × 100; remaining is native
  enrichment × 100. Core/skin temperature getters are native °C.
- ZIRNOX depletion = native rod lifetime / metadata variant maxLife × 100;
  remaining is its complement. Breeding rods retain their native state label.
- ZIRNOX °C = round(raw heat × 0.00001 × 780 + 20), matching its own GUI/API.
  Pressure bar = round(raw pressure × 0.00001 × 30).
- Native common-side container geometry determines slot IDs and coordinates;
  unexpected layouts fail closed rather than guessing.
- Only owned current-window endpoint IDs are accepted. Client fuel packets never
  supply trusted item stacks or arbitrary world coordinates. The authoritative
  cursor provides ghost selections. Rod commands validate binding, dimension,
  percent and a ten-tick request limit. Unattended actions use the owner's profile
  with world/spawn edit checks and cancellable right-click protection events.
- Registry saves links, bindings and configuration, not authoritative live fuel
  percentages. Existing old save fields and ordinary rules remain compatible.
- Detailed reactor snapshots refresh with the terminal's ten-tick update cadence
  while the relevant view is open. Catalog metadata is chunked (64 rows/packet)
  and sent only when changed. Existing terminal device limit remains 128.
- Default feeder polling: 20 server ticks, up to two successful transfers per
  endpoint poll, shared eight transfer attempts per server tick. Sources are
  bounded to 256 exposed slots; oversized inventories report unsupported.
  Endpoint scheduler visits remain bounded. Low TPS/large registries slow updates.

No external scripts, HBM copies, test harnesses, or client rendering classes are
introduced into dedicated-server reactor paths. See TESTING.md for performed checks.
