# Industrial Technology 1.5.1

Automation rules can now repeat while a condition remains true:

```text
WHEN FLUID "water" >= 200mb
EVERY 20s
THEN PULSE "WATER" 5s
END
```

The first true check triggers immediately. EVERY accepts whole positive s/m/h
durations. False stops and resets the repeat; missing data/targets pause it.
Saved deadlines survive unchanged saves and restarts, with no catch-up burst.
Existing programs without EVERY keep their original edge-triggered behavior.

Tooltips restore GUI light positions and flags before NEI's same-frame item
rendering. This fixes icons becoming dark on button hover in quarry/terminal
screens. SIMPLE's TRUE/FALSE label is now beside the rule number, clear of
the CODE button and rule-navigation buttons.

## Changed implementation

- automation/ScriptCompiler.java: optional validated EVERY line before THEN.
- automation/ScriptRule.java: overflow-bounded interval, next deadline,
  deterministic due checks and backward-compatible NBT fields.
- automation/ScriptEngine.java: bounded repeat scheduling and Save preservation.
- client/GuiTooltips.java: shared immediate restoration around vanilla tooltips.
- client/GuiMachine.java and GuiWirelessTerminal.java: use shared tooltip handling;
  terminal status label has its own row.
- client/MultilineCodeEditor.java: EVERY and s/m/h syntax highlighting.
- AutomationReference.java, automation_reference.json, tools/create_automation_docs.py,
  AUTOMATION_CODES.md, README.md and WIRELESS.md: shared timing reference, three
  additional projects and fifteen projects total in the book/Help/Markdown.
- build.gradle and core/IndustrialTechnology.java: version 1.5.1; optional NEI
  test-client flag. No new runtime dependency.
- ScriptCompilerTest, RepeatTimingTest, CodeIntegration, CodeClientTest,
  ClientTestProxy, NeiClientTest and TooltipClientChecks: meaningful parsing,
  timing, real fluid/pulse/restart, upload and NEI rendering regressions.
- docs/TESTING.md, versioned evidence and tools/audit_release.py: final verification.

See TESTING.md for all 337 recorded checks and remaining compatibility limits.
