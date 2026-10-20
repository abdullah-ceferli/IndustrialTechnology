# Verification — Industrial Technology 1.5.1

Verified on Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614.
The build uses the existing ForgeGradle 1.2 maintenance fork. Prior 1.5.0
coverage and limitations are preserved in TESTING-1.5.0.md.

## Recorded checks

| Run | Passing checks |
| --- | ---: |
| JUnit parser, timing, editor, packets, reference and quarry geometry/rate | 49 |
| Native production Forge + HBM X5808, initial / restart | 128 / 10 |
| Native production Forge standalone, initial / restart | 111 / 10 |
| Actual editor Minecraft client | 23 |
| Actual NEI client and hover pixel comparisons | 6 |
| Total | 337 |

All final reports end in Failed: 0. Standalone duplicates are retained as
separate compatibility runs; 216 distinct checks remain after excluding them.
Release archive entries must exactly match both native-server-tested copies
(ZIP container timestamps may differ). Run tools/audit_release.py to verify.

## EVERY behavior checked

- Seconds, minutes and hours parse into long server-tick intervals; exact
  20-minute / 20-hour boundaries use deterministic clock tests, not real waits.
- Stable 200 mB water emits three real five-second pulses with EVERY 6s under
  the normal scheduler, without a GUI or online player.
- First true triggers immediately; false clears the repeat deadline. Reaching
  true again starts fresh. Programs without EVERY retain once-per-edge behavior.
- Unavailable tank APIs and unavailable targets preserve the pending deadline.
  Overdue return runs once, with no burst of missed actions.
- Unchanged Save and actual dedicated-server restart retain the next deadline.
- Invalid durations, units, duplicates, overflow and corrupt saved fields are
  rejected. Failed Save retains the previous working program and timer.
- Existing native ore, quarry power, loader, ownership, placement, SIMPLE rules,
  HBM adapter and packet-isolation regressions all pass in the same runs.

## Actual client checks

The editor test types and uploads a four-line EVERY 20s program through the
real FML packet, enables it, reaches 200 mB water, and checks the actual pulse
and pending repeat deadline. It also covers selection, clipboard, invalid
draft retention, SIMPLE/CODE switching, guide pages and item conditions.

The NEI test uses official NotEnoughItems 1.0.5.120, CodeChickenCore 1.0.7.47,
and CodeChickenLib 1.1.3.140 development distributions, all outside the release.
At 1560x900 / GUI scale 3, it compares pixels of all twelve mod icons before
and during hover on terminal SIMPLE, terminal CODE Save, and Quarry Start.
A warm-up frame and fixed item list avoid NEI's initial list change; color and
depth buffers are cleared before captures. It also checks LIGHT0/LIGHT1 and
the lighting/color flags immediately after tooltip rendering, before the
outer drawScreen restoration. Both TRUE and FALSE are geometrically clear of
every visible button. Screenshots were visually inspected.

Evidence is in docs/test-results/1.5.1 and docs/screenshots/1.5.1.
Failed intermediate test-fixture runs remain separate under run-test-*;
they are not counted as passing final evidence.

## Reproduce

Use tools/build.ps1 for Java 8 and the project-local Gradle cache:

```powershell
.\tools\build.ps1 -Tasks @('test','build','testModJar','-PproductionTest','-PtestRunDir=run-test-production-1.5.0-hbm-verified')
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.5.1-hbm-fresh -WithHBM -Port 25579
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.5.1-standalone-fresh -Port 25580
.\tools\build.ps1 -Tasks @('runClient','-PtestClient','-PcodeClient','-PtestRunDir=run-test-code-client-1.5.1-fresh')
```

For NEI, copy the three official development dependencies into a fresh test
run's mods folder, then runClient with -PtestClient -PneiTest and that
-PtestRunDir. No NEI dependency is required or bundled by the mod. Always run
test/build again after development clients, because runClient replaces the
build output with its deobfuscated input. The final distributed JAR is reobfuscated.

## Practical limits

Intervals use game ticks at 20 TPS: server shutdown time does not count, and
lag or bounded polling can delay actions. An active pulse ignores repeated
PULSE requests; use a longer interval than pulse length for a visible OFF gap.
Yellow only controls machines that accept vanilla redstone. No chunk loading
or cross-dimension control is added.

Only the stated NEI version was installed in this regression. Other NEI forks,
graphics shader stacks and the user's entire modpack are unverified. Existing
HBM limits still apply: full assembled multiblocks, dynamic Chemical Factory
delegates, other forks and simultaneous remote-player sessions remain unverified.
