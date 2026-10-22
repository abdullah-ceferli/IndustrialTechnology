# Verification — Industrial Technology 1.6.0

Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614. Optional reactor integration
was checked against the user's installed HBM-NTM 1.0.27_X5808 production JAR.
Prior release coverage is preserved in TESTING-1.5.1.md.

| Performed run | Passing checks |
| --- | ---: |
| JUnit parser, timing, editor, packets, settings, search and quarry geometry/rate | 62 |
| Native production Forge + HBM, initial / actual restart | 153 / 11 |
| Native production Forge without HBM, initial / actual restart | 117 / 10 |
| Real HBM + NEI reactor client | 17 |
| Existing CODE editor and guide client | 23 |
| Existing ordinary wireless gameplay client | 19 |
| Existing standalone NEI hover client | 6 |
| Total recorded checks, including duplicate compatibility runs | 418 |

Every final report ends in Failed: 0. These counts include repeated base
regressions across the HBM and standalone servers; they are recorded executions,
not a claim that every one is a unique test. Evidence is in test-results/1.6.0
and screenshots/1.6.0. Intermediate failed fixture runs are not passing evidence.

## Native HBM checks

The server test uses HBM's actual multiblock fill helpers to create real ZIRNOX
core/port blocks and RBMK columns/console. It links actual placed colored torches.
It does not substitute fake reactor TileEntities or send invented fuel snapshots.

- Real ZIRNOX ports resolve their actual controller and the native container's
  24 fuel slots, in seven staggered rows. Metadata-specific uranium/plutonium
  lifetime readings produce the expected 25% and 50% depletion.
- Native ZIRNOX raw heat 50000 becomes 410 °C using its verified GUI conversion.
  RBMK actual yield gives 47.5% depletion and 52.5% remaining; independent core
  and skin temperatures read 420 °C and 310 °C at the initial snapshot.
- Reading new fuel does not initialize its live NBT. Removing fuel clears the
  icon/percentage while keeping the link. A real RBMK top access cap resolves.
- Authoritative ghost selection consumes no fuel and rejects non-fuel items.
  Native slot-specific insertion moves one real partially used adjacent-supply
  rod, preserving its complete NBT and metadata. Missing fuel waits/resumes;
  wrong or occupied rods stay untouched. Protection cancellation blocks supply.
- Transfer-contract checks independently cover source extraction restrictions,
  target insertion restrictions, occupied/self-inventory rejection, stack/NBT
  conservation, and source rollback when the destination rejects insertion.
- The console identifies two connected manual rods and one excluded automatic
  rod. Another disconnected nearby manual rod stays untouched. Mixed current
  states produce an average with MIXED. Commands 0/10/50/90/100 convert correctly
  to native extraction targets. Actual native ticks move both rods gradually.
- A canceled permission check produces honest partial success; malformed targets,
  stale bindings, foreign ownership and other player dimensions are rejected.
  An unloaded anchor remains unloaded. An actual restart restores links, native
  controller identities, assignments, moved fuel NBT and last rod request.

## Actual clients and rendering

The reactor client runs the same installed HBM JAR remapped only for MCP
development names, plus official NEI 1.0.5.120, CodeChickenCore 1.0.7.47 and
CodeChickenLib 1.1.3.140 development builds. None is bundled in the release.
It assembles real reactors on the integrated server, opens the actual terminal,
sends normal FML/window inventory packets, applies a real fuel ghost assignment,
enables local loading and verifies the transferred native rod. Slider and full
insert/withdraw packets update real HBM targets. Actual temperature readings
change with native ticks; screenshots are not static demonstration values.

The owned catalog contains 112 rows across all three tabs. Tests exercise
case-insensitive partial search, pages, clear, clipboard input, backspace,
Escape focus, no results, hidden selected devices, renaming and separate queries.
Every rendered reactor screenshot checks visible buttons against one another
and the actual player inventory slots. Feeder controls stay above NEI Options.
Grid/list screenshots cover all native pages. Screenshots were visually inspected.

At 1560×900 / GUI scale 3, actual NEI item icon pixels are identical before and
during hover on fuel Apply and rod Apply. The existing NEI regression separately
checks terminal SIMPLE/CODE and quarry buttons, immediate GUI lights/color,
and the SIMPLE TRUE/FALSE row. Existing editor tests upload and run EVERY code,
test real pulses, drafts, clipboard, errors, guide pagination and mode switching.
The ordinary wireless client checks real torch placement, chest/tank monitoring,
redstone power, automation, ownership, distant/offline links and inventory colors.

## Retained gameplay

Both native servers rerun the existing quarry ore, Fortune/Silk Touch, VOID,
protection, below-placement depth and RF regressions. At 20 TPS, 200 RF/t still
mines 1 block/s and 20000 RF/t reaches 100 blocks/s in every tier. Sparse chunk
tickets, releases, enabled/disabled restart state, SIMPLE, CODE and EVERY timing
remain covered. Dedicated-server execution verifies common paths without loading
client rendering classes; the standalone run has no HBM installed.

## Reproduce

```powershell
.\tools\build.ps1 -Tasks @('test','build','testModJar','-PproductionTest','-PtestRunDir=run-test-production-1.5.0-hbm-verified')
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.0-hbm-fresh -WithHBM -Port 25579
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.0-standalone-fresh -Port 25580
.\tools\prepare-reactor-client.ps1 -TestName run-test-reactor-client-fresh
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PreactorClient','-PtestRunDir=run-test-reactor-client-fresh')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PcodeClient','-PtestRunDir=run-test-code-client-fresh')
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PwirelessClient','-PtestRunDir=run-test-wireless-client-fresh')
.\tools\build.ps1 -Tasks @('test','build')
python tools/audit_release.py
```

Client preparation requires the existing local X5808 and the three official NEI
development JARs in .toolchain/nei. It uses the already-cached SpecialSource and
SRG-to-MCP mapping; it never edits the original HBM JAR. NEI-only regressions use
the three development JARs in a fresh test run with -PtestClient -PneiTest.
Development clients replace the main output JAR with their deobfuscated input;
the final test/build restores a production-reobfuscated distribution. Archive
entries must match the release copies used by both native servers exactly.

## Practical limits

No separate C depletion field exists in X5808. Normal depletion and °C are
separate real readings. ZIRNOX has reactor temperature, not individual rod
temperatures. Fuel loading needs a physically adjacent source inventory and
does not rearrange occupied rods. RBMK group control supports connected loaded
manual rods in the console view; automatic rods remain native. Decorative
console blocks without TileEntities are not access controllers. No target
chunk loading, cross-dimension reactor access, general HBM HE conversion or
reactor DSL commands are added. See REACTORS.md for setup and exact data formulas.

Other HBM forks, shader stacks, the full user modpack, unusual inventory contracts,
and simultaneous active remote-player sessions were not tested. Ownership and
protection were tested server-side with distinct player profiles and cancellable
events; that is not a multi-client load test. Large-registry throughput is bounded
but not a performance benchmark. Timing uses server ticks; low TPS delays updates.
