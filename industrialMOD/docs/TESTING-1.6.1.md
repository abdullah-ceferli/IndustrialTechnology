# Verification — Industrial Technology 1.6.1

Tested on Java 8 / Minecraft 1.7.10 / Forge 10.13.4.1614, with and without the
installed HBM 1.0.27_X5808. The final production jar is checked against both native
server copies. Prior 1.6.0 client evidence and limits are in TESTING-1.6.0.md.

| Performed run | Passing checks |
| --- | ---: |
| Existing JUnit parser, packets, settings, editor, search, geometry and rate | 62 |
| Native production Forge with HBM, initial / actual restart | 164 / 12 |
| Native production Forge without HBM, initial / actual restart | 128 / 11 |
| Total recorded checks, including duplicate compatibility runs | 377 |

Every final report ends in Failed: 0. Reports and archive/dependency hashes are
in test-results/1.6.1. These are recorded executions, including repeated base
regressions in the two server modes. Failed intermediate runs are not passing
evidence. No new actual GUI client run is claimed for this patch; its behavior
is tested through native server harvesting, physical pickup and placement,
inventory serialization and the real vanilla ItemStack network encoder/decoder.

## Quarry item preservation

The new native integration checks cover:

- Each of MK1/MK2/MK3 is configured with all 18/36/50 VOID entries, varied item
  metadata and custom NBT, enabled/ignore-metadata flags, a selected 3x3 area
  and Silk/Fortune choice. The quarry owns nine real forced chunks.
- A survival player uses the actual ItemInWorldManager harvest path with a
  diamond pickaxe. Exactly one quarry item appears; real diamond output and
  pending coal drop separately once. The original tickets are released.
- A physical EntityItem pickup puts the quarry in the player's inventory. The
  stack survives save NBT and a PacketBuffer ItemStack round trip. Actual
  ItemBlock.onItemUse placement restores every selection, including the final
  slot, and consumes the carried item.
- New placement uses the new height and owner, with a new machine identity.
  No old cursor, RF, statistics, output inventory, active tickets or running
  state is duplicated. The quarry remains stopped until Start.
- Fortune III and Silk Touch pickaxes each produce one correctly configured
  quarry. Creative breaking retains its normal no-block-drop behavior.
- Plain crafted items retain defaults. Drop queries hold independent NBT copies.
  Malformed item settings cannot inject RF/enabled state, invalid VOID slots or
  an invalid chunk selection. Canceled Forge breaks preserve the live machine,
  its settings and stored items without emitting drops.
- A full MK3 filter with 50 individually valid 800-byte random NBT payloads
  exercises the legacy packet-size limit. All entries survive a real break,
  vanilla item packet, placement, second break and storage in an actual chest.
  After a new server process loads that world, the chest's quarry item again
  places with all 50 NBT-rich filter entries intact.
- Missing large-filter profile references fail closed with an explicit issue.

Normal configurations are inline in the item. Only exceptionally large NBT
configurations use a deduplicated immutable world profile. Those items require
their world's saved profile data; cross-world transfer of profile-backed items
is not supported. Settings restore within the current chunk and Silk limits.
Crafting a tier upgrade is not a settings-preserving transfer operation.

## Retained coverage

Both servers rerun existing quarry power/rate/depth/ore, Fortune/Silk, VOID,
protection, sparse ticket and restart tests. 200 RF/t remains 1 block/s and
20000 RF/t remains 100 blocks/s at 20 TPS. Wireless ownership, placement,
ordinary monitoring, SIMPLE/CODE and EVERY continue passing. The HBM server
also reruns native reactor monitoring, Celsius/depletion readings, fuel supply,
manual control-rod motion, permissions and persistence. The standalone server
does not have HBM installed. Common code works on a dedicated server; the new
ItemBlock's client-only tooltip is stripped there.

The previous 1.6.0 reactor/search, CODE, ordinary wireless and NEI client tests
remain documented in TESTING-1.6.0.md. GUI layouts and rendering code are unchanged
in this patch. The newly added tooltip was not visually tested in a client.
Other HBM forks, simultaneous remote-player sessions, shader packs and the full
user modpack remain outside the tested coverage.

## Reproduce

```powershell
.\tools\build.ps1 -Tasks @('test','build','testModJar','-PproductionTest','-PtestRunDir=run-test-production-1.5.0-hbm-verified')
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.1-hbm-fresh -WithHBM -Port 25579
.\tools\verify-wireless-production.ps1 -TestName run-test-production-1.6.1-standalone-fresh -Port 25580
python tools/audit_release.py
```

Audit expects the retained final test directories and versioned reports; use fresh
run-test-* names for additional runs. The release must contain only production
classes/resources, Java 8 bytecode and the existing licensed RF API, with no
HBM, NEI, Forge, Minecraft or test-harness classes or nested jars bundled.
