> Current release: **2.1.0**. Read [SPACE_HANDOFF.md](SPACE_HANDOFF.md) for the latest Shuttle, LC-39, planetary resources and native HBM integration. The following pre-space snapshot is retained as historical background.

# Industrial Technology — project handoff for ChatGPT and Codex

Snapshot date: **20 October 2026** (Asia/Baku). Latest completed mod release: **1.5.1**.

## 1. What this document is for

The user is developing an existing Minecraft Forge mod with Codex. They want to give this document to ChatGPT so ChatGPT can write a clear prompt for the next development request.

Use this as factual project context. The next feature or change has **not yet been specified**. Do not treat the completed work below as a request to implement it again. Do not invent a new feature list. Ask the user what they want next, then write an implementation prompt that preserves the existing working features and states the required result and checks.

Local paths below refer to the user's Windows PC. A separate ChatGPT conversation cannot read those files unless the user supplies them. This document contains the essential context without requiring access to the old conversation or pasted specifications.

## 2. Current project and latest build

| Detail | Current value |
| --- | --- |
| Mod name / ID | Industrial Technology / `industrialtech` |
| Minecraft | **1.7.10** |
| Forge | **10.13.4.1614** |
| Java | **Java 8** |
| Mod version | **1.5.1** — this is the mod version, not the Minecraft version |
| Actual project folder | `D:\IndustrialTechnology\industrialMOD` |
| Parent Git repository | `D:\IndustrialTechnology` |
| Old workspace path | `D:\industrialMOD` — no longer exists; use the actual project folder |
| Main Java package | `dev.industrialtechnology` |
| Build | Gradle 7.4.2 with the existing ForgeGradle 1.2 maintenance fork, `1.2-1.1.1` |
| Latest recorded Git commit | `837d19a` — `fixed NEI bugs and ui bigs` |

The source working tree was clean when this handoff was started. This handoff is a newly added documentation file; it does not change the mod or rebuild its JAR.

Installable JAR:

```text
D:\IndustrialTechnology\industrialMOD\build\libs\IndustrialTechnology-1.7.10-1.5.1.jar
```

Developer sources JAR:

```text
D:\IndustrialTechnology\industrialMOD\build\libs\IndustrialTechnology-1.7.10-1.5.1-sources.jar
```

The installable JAR is **268,286 bytes**. Its SHA-256 was checked again for this handoff:

```text
da60da20db7ce3abea5a121fb3c3831cc7b73ec2f97094866cfa09c8755a106c
```

Version 1.5.1 was built and delivered. Installation and testing in the user's entire live modpack have not been confirmed. Install the main JAR on both client and server, replacing the old version; keep one version in each mods folder. The sources JAR is not the installable mod.

## 3. What is already implemented

### Quarries, mining and protection

- Three RF-powered quarries: **MK1, MK2 and MK3**.
- Default energy cost: **4,000 RF per successfully mined block**.
- Default aggregate input ceiling: **20,000 RF/t**, shared by all sides.
- Default maximum speed: **100 blocks/second at 20 TPS**, or five blocks per tick. At **200 RF/t**, the sustainable target is **one block/second**. Stored energy cannot bypass operation budgets.
- Fortune I/II/III by tier, and mutually exclusive Silk Touch using actual block harvesting behavior.
- Generic modded block/ore harvesting, including compatibility work for HBM ores. This is not a hardcoded vanilla-only ore list or a promise that every block from every mod is mineable.
- Supported ore TileEntities can preserve their resource data. Storage, RF/fluid machines and other protected infrastructure remain protected, even when registered as ores.
- Bedrock, unbreakable and protected blocks are skipped. Existing Forge mining/protection events and owner identity remain respected.
- Mining starts **below the quarry's placement Y**. The placement layer and higher layers are protected.
- Saved per-chunk progress; Start/Stop; output-space handling; safe drop capture; 54 output slots.
- A 5×5 chunk selection interface supporting precise selections of up to 25 chunks within server limits.

### Void Filters, chunk loading and visuals

- Void Filter capacities: **18 / 36 / 50** for MK1 / MK2 / MK3, with 25 entries per page.
- Ghost-item filters with exact identity or Ignore Metadata behavior, Auto Delete, Clear All and saved state. Existing output is not deleted by changing a filter.
- A separate **RF-free Chunk Loader** with no fuel or energy storage requirement.
- Exact selected-chunk loading and ticket ownership; stopped/completed quarries release their tickets.
- Public red world boundaries for active machines and exact loaded/selected areas, with local visibility controls.
- A small original cyan crystal above the chunk loader: rotating/bobbing when active and stationary when off.
- Existing industrial pixel-art textures and GUI styling. Earlier quarry/chunk-loader images served as visual references.

### Wireless Terminal and colored sticks

- Owner-bound, saved wireless networks; device selection, renaming, unlinking and status information.
- Terminal opens with left-click, with attack/break suppression while held; right-click also works.
- **Green Stick:** read-only inventory and tank monitoring.
- **Yellow Stick:** real vanilla redstone output, **0 OFF / 15 ON**, with manual and timed pulse operation.
- **Blue Stick:** watches a readable machine face and runs SIMPLE or CODE automation.
- All three sticks have the **same classic redstone-torch shape**, with green, yellow or blue tips. The user specifically requested torch-shaped color variants.
- All three are actual placeable torches: normal right-click on ground or machine faces; sneaking is optional. Placement prioritizes the stick over opening the machine.
- Placement works without a terminal. Carry a terminal for automatic linking, or link an existing torch later.
- Ground placement is allowed, but Green/Blue need a supported attached inventory/tank to read useful data.
- The registered face controls accessible slots/tanks. Monitoring does not transfer contents.
- Networks use no RF and **do not force-load chunks**. Devices must be loaded in the same dimension.
- Owner/network/window/dimension checks and bounded server packets protect remote actions.
- Ordinary survival recipes; HBM, AE2, Thermal Expansion and CoFHCore are not required to load the mod. A small licensed CoFH RF API is bundled.

### SIMPLE automation

- Up to **eight saved rules per Blue Stick**.
- Conditions: Contains item, Item amount reached, Inventory full, Inventory empty and Item missing.
- Ghost item, quantity, target Yellow Stick, enable/disable and Apply controls.
- SIMPLE item identity compares item, metadata and NBT.
- Conditions trigger on a false-to-true change. Stable true does not repeatedly activate a target.
- Uses the chosen Yellow Stick's own configured output mode and pulse duration.

### CODE editor and guide book

- Existing Automation tab has **SIMPLE / CODE** buttons. Both sets of rules remain saved; only the selected mode executes.
- Real multiline editor with Enter, Backspace/Delete, arrows, mouse selection, clipboard, undo/redo, scrolling, line numbers, syntax colors and line errors.
- Invalid Save preserves the draft and the previously working program. Save does not automatically enable disabled code.
- Server-side execution of cached compiled rules, with saved trigger state and bounded polling. No GUI needs to remain open.
- Restricted beginner-friendly language; no Java/Lua/JavaScript execution or external scripting engine.
- Craftable **Automation Guide**: shapeless **Book + Redstone + Paper**.
- Classic brown book item and cream Minecraft-style pages. Help opens the same reference without discarding the editor draft.
- Book, Help and generated `AUTOMATION_CODES.md` share one authoritative reference, with **15 example projects**.

## 4. Current CODE language — exact behavior

One rule without repetition:

```text
WHEN FLUID "water" >= 200mb
THEN PULSE "WATER" 5s
END
```

The Blue Stick watches the tank. The linked Yellow Stick must be named exactly `WATER` in the same network/dimension. At least 200 mB of water triggers a five-second pulse once. It must go below the threshold and reach it again for another trigger.

Repeating rule, added in 1.5.1:

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

The first true check triggers immediately. It then repeats every twenty seconds while true. Use `EVERY 20m` for twenty minutes or `EVERY 20h` for twenty hours. Put the optional EVERY line **between WHEN and THEN**.

| Syntax | Meaning |
| --- | --- |
| `WHEN ITEM "minecraft:stone" >= 64` | At least 64 of the registered item |
| `WHEN ITEM "minecraft:coal" < 16` | Fewer than 16 of the registered item |
| `WHEN INVENTORY EMPTY` / `FULL` | Accessible inventory state |
| `WHEN FLUID "water" >= 200mb` | At least 200 mB of matching fluid |
| `WHEN FLUID "water" < 500mb` | Less than 500 mB of matching fluid |
| `WHEN TANK EMPTY` / `FULL` | State of the exposed supported tank set |
| `EVERY 20s` / `20m` / `20h` | Optional repeat interval while true |
| `THEN PULSE "WATER" 5s` | Power 15 for five seconds, then OFF |
| `THEN PULSE "WATER"` | Use the Yellow Stick's configured pulse duration |
| `THEN ON "WATER"` | Stay powered until changed by a later command |
| `THEN OFF "WATER"` | Turn OFF immediately |
| `END` | Finish this rule |

Important details:

- **PULSE duration is 5–10 seconds. `THEN PULSE "pump" 2s` is invalid.** EVERY intervals and PULSE lengths are different settings.
- EVERY accepts whole positive durations with `s`, `m` or `h`; malformed/overflowing values are rejected. Its minimum is one second, but an interval shorter than a pulse does not create separate visible pulses.
- Active pulses ignore additional PULSE requests and do not extend their deadline. Choose a longer interval than pulse duration for an OFF gap.
- False resets the repeat timer. Missing data/offline targets pause affected rules rather than pretending that an inventory/tank contains zero.
- Unchanged saves and restarts preserve repeat deadlines. An overdue rule runs once when available again, with no burst of missed actions.
- Time uses server ticks: twenty ticks per second at normal TPS. Lag/polling may delay actions; server shutdown time does not count.
- ON/OFF cancel an active pulse timer. Without EVERY, rules retain once-per-edge behavior.
- At most eight rules; default maximum script length is 4,096 characters, configurable within 256–8,192. One condition and one action per rule; no general loops, variables, ELSE or compound AND/OR language.
- Keywords ignore case; quoted control names are exact. Duplicate names are rejected; authorized `id:UUID` control references are available.
- CODE item counts include metadata/NBT variants of the registered item; SIMPLE supports exact ghost-item identity.
- Matching fluid quantities are summed across the exposed tanks. Hover a monitored tank for its recognized Fluid ID; do not guess mod-fluid IDs.
- If several actions for one target are due in the same script poll, the last block in text order wins.

## 5. Problems already fixed

| Earlier problem | Completed fix |
| --- | --- |
| Quarry skipped mod ores, especially noticed with HBM | Generic harvesting/tool compatibility and eligible ore TileEntity handling, with protection retained |
| Quarry was too fast at 200 RF/t | 4,000 RF/block and the 20,000 RF/t → 100 blocks/s balance |
| Sticks had the wrong requested appearance | Matching classic torch silhouettes with color variants |
| Sticks could not be placed/used on ground or machine ports | Client placement packet fix, normal right-click, physical Green/Blue/Yellow torches, later linking |
| Needed accessible automation code and documentation | CODE editor, restricted language, shared guide/Help/Markdown reference |
| True water condition only triggered once | Optional EVERY repetition in 1.5.1, preserving old behavior without it |
| NEI icons became dark while hovering buttons | Immediate tooltip lighting-state restoration before NEI renders its icons in the same frame |
| Terminal TRUE/FALSE overlapped CODE | Moved beside the rule number, clear of mode/navigation buttons |

Main milestones: **1.4.0** wireless/filter features; **1.4.1** usable physical stick placement; **1.5.0** CODE editor and guide; **1.5.1** EVERY and the two visual fixes.

The latest reported issues have been addressed in the built release. Do not assume a new bug report, deployment confirmation or new feature request beyond this handoff request.

## 6. What was actually verified

Latest release evidence records **337 passing checks**, with no failures in the final recorded runs:

| Run | Passing checks |
| --- | ---: |
| Unit tests | 49 |
| Native Forge + HBM X5808, initial / restart | 128 / 10 |
| Native standalone Forge, initial / restart | 111 / 10 |
| Actual editor Minecraft client | 23 |
| Actual NEI rendering client | 6 |

This includes repeated scenarios in the standalone compatibility run: **216 distinct checks** remain after excluding those duplicates. Do not add old release totals to this count.

Real server checks included repeated five-second pulses with `EVERY 6s` while water remained at 200 mB, saved timers, false/unavailable states, restart behavior and existing quarry/loader/ore/wireless regressions. Twenty-minute and twenty-hour boundaries were checked with deterministic clock tests, not by waiting those real durations.

The editor client typed and uploaded an EVERY program through actual packets and verified saved timers. NEI checks used **NotEnoughItems 1.0.5.120**, **CodeChickenCore 1.0.7.47** and **CodeChickenLib 1.1.3.140** development distributions, including before/hover icon pixel comparisons. They were test dependencies, not bundled runtime dependencies.

The final release's archive entries match the copies exercised by both native server runs. The audit confirms Java 8 classes and no bundled test harness, Minecraft/Forge/HBM/NEI implementation classes or nested mod JARs.

## 7. Limits and unverified compatibility

- Generic compatibility is not a guarantee for all modded machines, ores, ports or protection plugins.
- HBM monitoring targets inspected **1.0.27_X5808** APIs. Actual Chemical Plant, Fluid Tank and Big Ass Tank controller classes were checked, including 100 → 200 mB conditions.
- Full assembled HBM multiblocks, dynamic Chemical Factory delegate ports and other HBM forks remain unverified. A supported controller-class test does not prove every assembled structure/port works.
- Arc Furnace inventories can be monitored; its molten-material lists are not presented as supported fluid tanks.
- Yellow sends vanilla redstone. It cannot add control support to a machine that ignores vanilla redstone.
- No built-in generator, direct HBM HE conversion, general HBM machine-control API, remote item/fluid transfer, shared ownership or cross-dimension wireless control has been added.
- Other NEI forks, shader stacks, full modpack performance/soak tests and simultaneous remote-player sessions remain unverified.
- Unsupported or broken machine APIs pause affected rules safely. Hidden custom slot limits can prevent exact inventory-full inference.

The user explicitly wants to be told when requested behavior is unsupported or cannot be verified. Keep that disclosure clear; do not present an API-based implementation as universal modpack verification.

## 8. Where future Codex work should begin

Paths in this table are relative to `D:\IndustrialTechnology\industrialMOD`.

| Location | Responsibility |
| --- | --- |
| `src/main/java/dev/industrialtechnology/quarry/` | Mining, rates, harvesting, protection, filters and boundaries |
| `src/main/java/dev/industrialtechnology/chunkloading/` | Utility loader and Forge tickets |
| `src/main/java/dev/industrialtechnology/wireless/` | Networks, sticks/relays, monitoring, SIMPLE rules, scheduler and terminal container |
| `src/main/java/dev/industrialtechnology/automation/ScriptCompiler.java` | CODE grammar and line errors |
| `automation/ScriptRule.java` and `ScriptEngine.java` in that package | Conditions/actions, saved edge/timer state and execution |
| `automation/ScriptUpload.java` and `ScriptDocument.java` in that package | Bounded script upload/document packets |
| `src/main/java/dev/industrialtechnology/client/` | Terminal/editor/guide GUIs, renderer state and overlays |
| `client/GuiTooltips.java` in that package | Same-frame tooltip lighting restoration used by machine/terminal screens |
| `src/main/resources/assets/industrialtech/automation_reference.json` | Shared book/Help/Markdown command reference |
| `src/test/java/` and `src/testMod/java/` | Unit tests and actual server/client integration harnesses |
| `AUTOMATION_CODES.md`, `README.md`, `docs/WIRELESS.md` | Current usage and command documentation |
| `docs/CHANGES-1.5.1.md`, `docs/TESTING.md` | Latest changes, verified coverage and practical limits |
| `docs/test-results/1.5.1/`, `docs/screenshots/1.5.1/` | Final reports, audit/hash and screenshots |

Package shorthand in the table refers to the corresponding full package path above, not an extra root directory. Inspect the actual files before making changes; this is a snapshot.

Build from the actual project folder in PowerShell:

```powershell
.\tools\build.ps1 -Tasks @('test','build')
```

The helper selects the project's Java 8 toolchain and local Gradle cache. The local JDK is under `.toolchain\jdk8\jdk8u504-b01`.

Other existing tools: `tools/verify-wireless-production.ps1` for disposable native server/restart runs, `tools/audit_release.py` for release/evidence checks, and `tools/create_automation_docs.py` to regenerate the Markdown reference after editing its JSON source. Exact run setup is in `docs/TESTING.md`.

**Run the production build again after development `runClient`/`runServer` sessions before distributing a JAR.** Those tasks can replace the main build JAR with deobfuscated development input. Keep test harnesses and third-party test mods out of the release.

The app may still configure the missing old workspace path. Use the actual project location and the normal permission mechanism where required; do not recreate the old folder as a separate project or bypass access restrictions.

## 9. Requirements to carry into the next prompt

- Continue the **existing mod**, preserving Minecraft 1.7.10 / Forge / Java 8 compatibility. Do not start over or migrate Minecraft unless the user explicitly requests it.
- Preserve saved IDs, networks, inventory/output, filters, cursors, tickets and automation state. Preserve quarry balance and the RF-free utility loader.
- Keep the existing industrial visual style and matching colored torch designs unless asked to change them.
- Preserve server-authoritative ownership, protection, packet bounds and read-only monitoring behavior.
- If changing CODE, update the parser/runtime, editor highlighting, shared JSON reference, generated Markdown and relevant tests together.
- Build a usable replacement JAR and report relevant checks, behavior changes and remaining limits. Do not claim it has been installed in the user's live pack without confirmation.
- Explain new commands plainly, with a copyable example. The user prefers practical language and concrete results.
- Do not rerun old one-time editing scripts or overwrite unrelated user changes. Read current source and repository instructions first.

The next implementation prompt should combine these constraints with the user's **new requested change**. This document itself does not request any additional mod feature.
