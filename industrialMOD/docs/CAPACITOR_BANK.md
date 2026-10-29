# Advanced Capacitor Bank — 2.5.0

Place banks touching through faces. They automatically form a shared RF battery in any shape;
diagonal contact alone does not connect them. No controller or wiring between banks is needed.

| Loaded banks | Capacity | Shared input | Shared output |
| --- | ---: | ---: | ---: |
| 1 | 25,000,000 RF | 25,000 RF/t | 25,000 RF/t |
| 2 | 50,000,000 RF | 50,000 RF/t | 50,000 RF/t |
| 5 | 125,000,000 RF | 125,000 RF/t | 125,000 RF/t |
| 20 | 500,000,000 RF | 500,000 RF/t | 500,000 RF/t |

Connect an RF generator to an Input or Both face, then connect a machine or compatible
energy cable to an Output or Both face on any member. Both is the initial face mode.
One bank can supply the Quarry's 20,000 RF/t operating target. It generates no energy.
HBM HE requires an actual RF converter; direct HE transfer is not implemented.

Right-click a member for its GUI. All members show the same shared RF and statistics.
IN and OUT boxes are whole RF/t values; Set applies that network limit. Max restores Auto,
so the limit scales with the number of loaded banks. A value of 0 stops that direction.
Current RF/t values measure the previous completed server tick, not a predicted generation rate.

SIDE CONFIG changes only the selected bank's face. Directions are absolute world directions;
the front direction is shown below the diagram. Input, Output, Both and Disabled govern external
RF; touching banks remain connected in every mode. Input faces show a green +, output faces a red minus, and disabled faces retain the locked symbol. The GUI uses the same input/output colors. A full bank turns its GUI charge bar, stored-RF number and enabled world gauge gold; nearly full charge stays in the ordinary gauge color. Original Ender IO casing and border textures remain intact. Newly placed and upgraded banks show the normal plain casing by default.
Sneak + right-click a Both face with an empty hand to toggle its optional charge gauge. Normal right-click
still opens the GUI. Each face remembers its own display choice through save/reload and charged-item
replacement. The gauge uses the SINGLE strip of the original atlas, not the whole atlas across a face.
Existing 2.3.0 banks without a saved display choice also default to the normal casing.

Redstone modes control the whole component's **output**. Input can continue while paused.
Always Active ignores signals; Active with Signal requires at least one powered loaded bank;
Active without Signal requires none. Signals from unavailable chunks do not count.

Crafting, in a normal 3x3 table:

```text
Diamond      Gold block       Diamond
Circuit      Redstone block   Circuit
Diamond      Gold block       Diamond
```

This uses four diamonds, two gold blocks, two existing Industrial Technology circuits and one
redstone block. It produces one empty bank and requires no optional mod ingredients.

Use a suitable pickaxe in survival to retain that bank's own RF, its face settings and shared
control settings. Only its local saved RF moves into the item, not the complete network balance.
Empty banks stack to 64 when their saved settings and item data match. Freshly crafted and unconfigured empty drops stack together; older default-empty items normalize when carried in a player inventory. Charged banks remain single items, each retaining its own RF. Configured empty banks keep their settings when placed from a stack. Replacing a bank consumes one item normally.
Creative destruction discards RF. Explosions can destroy the item; do not use explosions to move storage.

On a split, each remaining component retains the RF physically owned by its members. RF need
not be divided evenly: input fills available physical cells and shared extraction drains them.
On a merge, the component with the highest saved settings revision wins; ties use the first
bank in coordinate order. Limits are effectively clamped to current physical capacity; Auto grows
with the network. Limits/redstone apply to the loaded component, while face settings stay local.

Banks do not load chunks. An unloaded bank keeps its own saved RF but contributes no available
energy, capacity or rate. Components rejoin when their chunks load. Normal server restarts restore
each cell and reconstruct topology. A server crash has normal Minecraft chunk-save limitations;
there is no cross-chunk transactional crash journal.

Server config is in the existing `industrialtech.cfg`, category `capacitor`:

| Key | Default | Range |
| --- | ---: | --- |
| capacityPerBank | 25000000 | 1 to 1000000000000 RF |
| inputPerBank | 25000 | 1 to 1000000000 RF/t |
| outputPerBank | 25000 | 1 to 1000000000 RF/t |
| indicatorUpdateTicks | 10 | 5 to 100 ticks |
| guiUpdateTicks | 10 | 5 to 100 ticks |

Restart after config edits. Lowering capacity below already saved local RF clamps that cell to the
new capacity on load. Aggregate totals use 64-bit longs; RF API int getters saturate at 2,147,483,647.
Individual API calls retain their normal int transfer size; multiple calls share the long network budget.

Topology searches happen on placement, removal and bank chunk transitions. Membership, receivers,
redstone counts and component totals are cached. The server does not scan every member each tick.
Only a changed visible charge step updates member rendering (at most once per configured interval).
No emitted lighting, forced chunk tickets or per-frame bank TileEntity renderer is used.

Compatible receiver callbacks are debited before the call; rejected RF is refunded to its original
cells and output quota. Reentrant input/extraction on the same network is blocked for that transaction.
If another mod throws after an unknown partial acceptance, the offered RF is conservatively consumed
and logged rather than risking duplication. This is an exceptional broken-receiver case.

The block uses seven unchanged PNGs from the user's Ender IO 1.7.10 2.3.0.430_beta installation.
Credit: CrazyPants and Ender IO contributors. See `THIRD_PARTY.md` and the bundled original LICENSE/NOTICE.
Its independent controls and scalable RF implementation are part of Industrial Technology.
