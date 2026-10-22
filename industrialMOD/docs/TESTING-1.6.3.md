# Verification — Industrial Technology 1.6.3

Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614. Actual reactor integration
uses the installed HBM 1.0.27_X5808. Previous evidence remains in TESTING-1.6.2.md.

| Performed run | Passing checks |
| --- | ---: |
| JUnit settings, packets, parser, timing, editor, geometry and rate | 62 |
| Native production Forge + HBM, initial / actual restart | 165 / 12 |
| Native production Forge without HBM, initial / actual restart | 128 / 11 |
| Actual HBM + NEI reactor/search/pattern client | 27 |
| Total recorded checks, including duplicate compatibility runs | 405 |

All final reports end in Failed: 0. Counts describe performed executions,
including repeated compatibility checks. Captured reports are in test-results/1.6.3;
screenshots are in screenshots/1.6.3. The archive audit requires every entry in
the distributed jar to match both native production-tested copies exactly.

## Fuel order and actual/planned display

The actual client creates native HBM multiblocks and uses real wireless packets.
It reproduces seven natural uranium rods in the first two native rows while
all 24 positions have a mixed uranium/thorium assignment, including a wrong
installed rod at row 2, column 2. The terminal reports seven installed rods,
17 empty/planned positions and one mismatch. Installed and assigned tooltip
names are separate. The source contains only natural uranium, while the empty
assigned positions need thorium; live diagnostics correctly report NO MATCHING FUEL.

The client opens HBM's actual reactor GUI through the server's native GUI handler.
Every one of its 24 inventory slots is compared against the terminal fixture.
Screenshots capture both screens. Source and fuel tooltips wrap within the viewport.
Planned icons are visibly dim with P badges;
the wrong installed rod gets a red !. All 24 positions remain visible on one page.

The second fixture starts with an empty core and 24 real rods in its immediately
adjacent chest. Six natural uranium and 18 thorium rods are assigned to individual
native positions. The ordinary server scheduler fills them over real game ticks,
with the normal two-transfer/poll limit. Every final native position must contain
the assigned metadata. All 24 unique NBT markers must survive and each original
source position must be empty. Both the completed terminal and HBM's own GUI are
captured and checked, so the result does not rely only on the adapter's own labels.

## Source selection and retained behavior

The production HBM server places an empty inventory earlier in Auto's side order
and a stocked inventory later. Diagnostics and actual transfer must select the
stocked source and the exact assigned native destination. Choosing the empty side
explicitly must keep using that side and leave the other source untouched.
Existing denied-source tests still pass. No occupied/wrong rod is overwritten,
and source extraction/destination insertion restrictions remain enforced.

The actual client also reruns native Celsius/depletion, all-grid clicks, Inventory
drawer and real cursor ghost selection, Apply/Enable, SIMPLE/CODE access, native
RBMK manual rod motion commands, multi-tab search/rename and real NEI icon pixel
comparisons during reactor Apply hover. Screenshots are visually reviewed.
The new source/fuel tooltip screenshots additionally preserve identical NEI icon
region pixels; those derived image comparisons are recorded in tooltip-pixels.txt.

Both native dedicated servers rerun the full existing quarry/wireless regressions
and an actual JVM restart, including saved quarry items, VOID filters, RF rates,
physical sticks, automation, sparse chunk tickets and ownership. The standalone
run includes no HBM jar. Test-only code and optional mod dependencies are excluded
from the distributed jar; all production classes target Java 8.

## Reproduce

```powershell
.\tools\build.ps1 -Tasks @('test','build','testModJar','-PproductionTest','-PtestRunDir=run-test-production-1.5.0-hbm-verified')
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.3-hbm-fresh -WithHBM -Port 25579
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.3-standalone-fresh -Port 25580
.\tools\prepare-reactor-client.ps1 -TestName run-test-reactor-client-fresh
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PreactorClient','-PtestRunDir=run-test-reactor-client-fresh')
.\tools\build.ps1 -Tasks @('test','build')
python tools/audit_release.py
```

The retained final native runs use -final and the client uses -review. The archive audit
reads those captured native payloads and the versioned reports. Final test/build
after development clients restores the reobfuscated distribution jar.

## Limits

Thermal Dynamics itself is not installed in these runs. The seven-rod fixture
reproduces sequential direct insertion into native HBM inventory; it does not
claim a Thermal Dynamics transport simulation. This mod does not intercept pipes
inserting directly into HBM, and it never rearranges occupied native fuel. Pipes
must deliver to a chest touching the Blue Stick's linked access block. Every
assigned fuel type must actually be available. Other HBM forks, shaders and the
complete user modpack are not verified. The independent CODE/book, ordinary
wireless and standalone NEI clients from 1.6.2 are retained as historical evidence,
not counted as executions of this release. The current reactor client runs with NEI.
