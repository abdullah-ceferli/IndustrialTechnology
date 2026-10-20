# Industrial Technology 1.5.0 — beginner automation code and guide

The Wireless Terminal Automation tab now offers SIMPLE and CODE. CODE replaces
the old rule widgets with a real multiline editor, while retaining separate
saved SIMPLE rules. Only the selected mode executes. The editor supports Enter,
Backspace/Delete, arrows, mouse selection, clipboard, scrolling, line numbers,
syntax colors, undo/redo, and useful line errors. Invalid saves preserve both
the draft and previously working program.

The restricted WHEN / THEN / END language has item thresholds, inventory
empty/full, fluid thresholds in actual mB, tank empty/full, and real Yellow
Stick PULSE/ON/OFF actions. At most eight rules run, with saved edge latches,
server timers, owner/network validation, bounded uploads and read budgets.
Compiled programs are cached; full source is synchronized only on selection
or revision changes. Saved registry IDs are validated once after loading.
Unavailable data pauses rules, and old IDs from removed mods cannot become
false zero-resource readings.

The normal leather Automation Guide is craftable from Book + Redstone + Paper.
It uses classic Minecraft parchment, page arrows, and a copyable example
button. Editor Help uses the same book while preserving the terminal draft.
Its pages and the complete root AUTOMATION_CODES.md come from one authoritative
JSON reference. All documented examples are checked against the parser; the
build checks the Markdown reference hash.

## Files changed

Paths are relative to the existing project. The pre-existing user edit to
`.gitignore` remains untouched.

- `src/main/java/dev/industrialtechnology/automation/`: restricted compiler,
  compiled rules, registry/fluid identity, execution, bounded packets,
  authoritative reference reader, and guide item.
- `client/CodeEditorModel.java`, `MultilineCodeEditor.java`,
  `GuiAutomationGuide.java`: real text editing and classic paged book.
- `client/GuiWirelessTerminal.java`, `ClientProxy.java`: CODE/SIMPLE layout,
  draft preservation, Help, fluid-ID tooltips, and queued document updates.
- `wireless/WirelessEndpoint.java`, `WirelessScheduler.java`,
  `WirelessConfig.java`, `WirelessAccess.java`, `WirelessCommand.java`,
  `TerminalContainer.java`: saved programs, exclusive mode execution, finite
  settings, redstone actions, slot safety, and server-authorized saves.
- `wireless/MachineInspector.java`, `HbmMonitoring.java`: actual exposed fluid
  IDs, complete tank sets, and safe unavailable-data handling.
- `core/{IndustrialTechnology,CommonProxy,ModRegistry}.java`: lifecycle,
  packet registrations, ordinary guide item and shapeless recipe.
- `src/main/resources/assets/industrialtech/automation_reference.json`,
  `textures/items/automation_guide.png`, `lang/en_US.lang`: shared command
  reference, original leather book pixel icon, and item name.
- `tools/create_automation_docs.py`, `create_wireless_assets.py`,
  `verify-wireless-production.ps1`, `audit_release.py`, `build.gradle`:
  documentation/art generation, reference consistency and release checks.
- `src/test/java/dev/industrialtechnology/{automation,client}/`: parser,
  document grammar, packet bounds/UTF-8, and multiline text-state tests.
- `src/testMod/java/dev/industrialtechnology/test/{CodeIntegration,
  CodeClientTest,WirelessTestFixtures,WirelessIntegration,ClientTestProxy}.java`:
  actual fluid/item scripts, HBM controller reads, source/edge persistence,
  malformed saves, real GUI input, book pages, and regression launches.
- `AUTOMATION_CODES.md`, `README.md`, `docs/WIRELESS.md`, `docs/TESTING.md`,
  `docs/TESTING-1.4.1.md`, `docs/screenshots/1.5.0/`,
  `docs/test-results/1.5.0/`: complete user and verified release records.

## Compatibility and limits

Existing quarry tiers, generic modded ore harvesting, 18/36/50 filters, physical
sticks, RF-free chunk loader, and the 20,000 RF/t → 100 blocks/second target are
preserved. Install the replacement JAR on both client/server; keep one version.
Existing saves do not need a reset.

Actual X5808 Chemical Plant, Fluid Tank, and Big Ass Tank classes pass fluid
code thresholds. Existing proxy tests remain covered. Complete assembled HBM
multiblocks, dynamic Chemical Factory delegates, other forks, and individual
machines' acceptance of vanilla redstone remain unverified. Arc Furnace molten
materials are not these fluid tanks. Unsupported APIs show unavailable status.
No arbitrary Java/Lua/JavaScript, OS commands, item/fluid transfer, HE conversion,
cross-dimension actions or implicit chunk loading are added.

See [TESTING.md](TESTING.md) for exact final build/runtime results and preserved
evidence, and [AUTOMATION_CODES.md](../AUTOMATION_CODES.md) for every command.
