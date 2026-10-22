# Verification — Industrial Technology 1.6.2

Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614. Optional reactor checks use the
installed HBM 1.0.27_X5808. Previous evidence is retained in TESTING-1.6.1.md.

| Performed run | Passing checks |
| --- | ---: |
| Existing JUnit settings, packets, parser, timing, editor, geometry and rate | 62 |
| Native production Forge + HBM, initial / actual restart | 164 / 12 |
| Native production Forge without HBM, initial / actual restart | 128 / 11 |
| Actual HBM + NEI reactor/search client | 19 |
| Actual CODE/editor/guide client | 23 |
| Actual ordinary wireless gameplay client | 19 |
| Actual standalone NEI hover client | 6 |
| Total recorded checks, including duplicate compatibility runs | 444 |

All final reports end in Failed: 0. These are recorded executions, including
duplicate compatibility checks. Evidence is in test-results/1.6.2 and
screenshots/1.6.2. Native server copies must have exactly the same archive
payload as the final distributed jar; tools/audit_release.py verifies that.

## Complete ZIRNOX grid

The real HBM client assembles native reactors and uses actual terminal packets.
It verifies all 24 native positions are inside the visible panel simultaneously,
with non-overlapping 18-pixel slot frames and 16-pixel icons. Grid page buttons
are hidden. Clicking each actual slot, including every position in the seventh
row, selects its matching native slot ID. The optional List retains all six
pages. Screenshots were visually inspected at 960x720 / GUI scale 3.

The Blue Inventory drawer uses the player's actual native inventory slots.
A real mouse click picks up the fuel and returns automatically to the full
grid. Clicking the ghost sends the real vanilla inventory packet and saves
metadata without consuming the held rod. The drawer returns that real item to
the player's inventory. Applying and enabling still moves a real adjacent-supply
rod into the correct native reactor slot with its complete NBT intact.

All rendered reactor screenshots check buttons against one another and every
visible player inventory slot. Green grid hides overlapping inventory slots;
List, SIMPLE, CODE and other devices restore normal slot positions. The drawer
shows its own compact inventory and suppresses overlapping grid controls. The
device-count label is removed in every terminal tab; search and list paging
continue working, including 112 linked catalog entries in this client fixture.

The same client checks Celsius/depletion, manual rod commands, renames, separate
tab queries, clipboard/focus and selected devices hidden by filters. At 1560x900,
actual NEI icon pixels stay identical before/during fuel Apply and rod Apply
hover. The separate NEI client verifies SIMPLE/CODE/quarry tooltips and GUI light
restoration. The CODE client types/uploads/runs EVERY programs, opens the updated
guide and verifies all book pages fit. Ordinary wireless gameplay verifies
physical sticks, chest/tank views, real redstone, ghost items and inventory colors.

## Retained server behavior

Both native dedicated servers rerun the full existing quarry and automation
regressions, including 200 RF/t = 1 block/s and 20000 RF/t = 100 blocks/s at 20 TPS,
generic ore/protection behavior, VOID filters, sparse chunk tickets and restart.
The 1.6.1 quarry item tests again verify native break/pickup/packet/placement for
all three tiers, complete 18/36/50-entry filters, settings, separate output items,
ticket cleanup, protection, and an actual restart with a stored NBT-rich quarry
item. The HBM server also checks native fuel readings, Celsius conversion,
local transfers, real rod motion, ownership, blocked rods and persistence.

No HBM/NEI dependency is bundled or needed by ordinary standalone operation.
Dedicated servers load common code without the client rendering paths. The new
inventory drawer is a client layout of existing owned slots, with no new remote
inventory permission or packet action.

## Reproduce

```powershell
.\tools\build.ps1 -Tasks @('test','build','testModJar','-PproductionTest','-PtestRunDir=run-test-production-1.5.0-hbm-verified')
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.2-hbm-fresh -WithHBM -Port 25579
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.2-standalone-fresh -Port 25580
.\tools\prepare-reactor-client.ps1 -TestName run-test-reactor-client-fresh
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PreactorClient','-PtestRunDir=run-test-reactor-client-fresh')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PcodeClient','-PtestRunDir=run-test-code-client-fresh')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PwirelessClient','-PtestRunDir=run-test-wireless-client-fresh')
.\tools\build.ps1 -Tasks @('test','build')
python tools/audit_release.py
```

For the NEI-only run, copy the existing official development jars into a fresh
test run's mods folder and use -PtestClient -PneiTest. Final test/build is needed
after development clients to restore the reobfuscated distribution jar. Audit
uses the retained final native test directories and versioned captured reports.

## Limits

Other HBM forks, shader stacks, simultaneous active remote-player sessions and
the complete user modpack are not verified. Native HBM data/motion, protection,
unloaded chunks and local supply limits remain as described in REACTORS.md.
Normal quarry configurations travel inline; exceptionally large NBT filter
profiles require their world's saved profile data, as documented in CHANGES-1.6.1.md.
Grid positions were tested at the default GUI scale and the larger NEI test
resolution. The terminal's overall size and the user's GUI scale are unchanged.
