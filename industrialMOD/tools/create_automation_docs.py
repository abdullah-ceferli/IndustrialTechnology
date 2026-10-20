"""Generate the complete player guide from the same reference used by editor Help/book."""
import hashlib,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
source=ROOT/'src/main/resources/assets/industrialtech/automation_reference.json'
data=json.loads(source.read_text(encoding='utf-8'))
lines=['# Automation Codes — Industrial Technology 1.5.1','',
       '<!-- Reference SHA256: '+hashlib.sha256(source.read_bytes()).hexdigest()+' -->','',
       'This is the complete reference for the Wireless Terminal SIMPLE/CODE system.',
       'The in-game Automation Guide and editor Help use the same reference data.','']
for row in data['intro']:
    lines+=['## '+row['title'],'',row['text'],'']
    if row.get('example'):lines+=['```text',row['example'],'```','']
lines+=['## Code words','', '| Word | Meaning |','| --- | --- |']
for row in data['glossary']:lines+=['| `'+row['word']+'` | '+row['text']+' |']
lines+=['','## Full command reference','']
for group in ['conditions','timing','actions']:
    for row in data[group]:
        lines+=['### '+row['id'].replace('_',' '),'','Syntax:','', '```text',row['syntax'],'```','',row['text'],'','Working example:','','```text',row['example'],'```','','Expected result: '+row['result'],'','Limits: '+row['limits'],'']
lines+=['## Example projects','','Before using an example, give your Yellow Stick the quoted name in Controls.','For oil and other mod fluids, the named fluid must actually be installed.','']
for i,row in enumerate(data['examples'],1):lines+=['### '+str(i)+'. '+row['title'],'','```text',row['example'],'```','',row['text'],'']
lines+=['## Combining rules','','```text','\n\n'.join(row['example'] for row in data['examples'][:3]),'```','',
        'Each block runs independently, up to eight. Only the selected SIMPLE or CODE mode runs.',
        'A pulse can replace manual ON; it uses its own deadline without changing the saved mode.',
        'A later ON or OFF cancels the timer. An active pulse ignores another pulse.',
        'Within one poll, the last due block for the same target wins. Stable true conditions',
        'without EVERY do not repeatedly fire. Different Blue devices use deterministic visit order.',
        'EVERY repeats from each due check while true. False resets it; unavailable targets keep',
        'the saved deadline. Overdue resume runs once and schedules from now, without bursts.',
        'Unchanged saves and restarts preserve timers. Shutdown time does not advance game ticks.','',
        'New or edited rules start unlatched. Existing unchanged rules, chunk reloads, and server',
        'restarts preserve latches. Offline/unknown affected rules keep their previous latch.',
        'A condition that first becomes true while its target is offline can fire once on resume;',
        'a previously latched true condition does not fire again merely because the chunk reloads.','',
        'Controls are compiled to authorized UUIDs. Renaming a control keeps a saved program working;',
        'update its quoted name before saving the program again. For ambiguous names use',
        '`"id:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"` from the Controls tooltip.','',
        'Keywords ignore case; quoted IDs/names are exact except unqualified HBM fluid name matching.',
        'Escape a quote inside a name as `\\"` and a backslash as `\\\\`.','',
        'The default script limit is 4096 characters, configurable from 256 to 8192. Uploads have',
        'an additional 24576-byte UTF-8 bound. At most eight finite rules run; no loops or OS calls.',
        'Polling defaults to ten server ticks; large registries and low TPS can update more slowly.','',
        '## Editor keys','',
        'Enter: new line. Backspace/Delete: remove text or the selection. Arrow keys: move the cursor.',
        'Shift + arrows: select. Home/End and Page Up/Down: move around. Ctrl+A: select all.',
        'Ctrl+C/X/V: copy, cut, paste. Ctrl+Z/Y: undo/redo. Mouse: place cursor or drag a selection.',
        'Mouse wheel: scroll. Long lines scroll horizontally to keep the cursor visible.',
        'Code input captures normal typing so movement/hotbar keys do not act on the game.',
        'Help opens the ordinary book reference and preserves your editor draft.','',
        '## Craft the Automation Guide','',
        'Shapeless recipe: one ordinary Book + one Redstone + one Paper = one Automation Guide.',
        'Right-click the brown leather book to read. Use the page arrows; Done closes it.',
        'It uses the classic Minecraft book page texture, with cream pages and dark text.','',
        '## Troubleshooting','']
for row in data['troubleshooting']:lines+=['### '+row['title'],'',row['text'],'']
lines+=['## Machine compatibility','',
        'Forge IInventory/ISidedInventory and IFluidHandler are supported. The clicked face defines',
        'accessible slots/tanks. Monitoring is read-only; no fill/drain/transfer calls are made.',
        'For multiple tanks, named-fluid quantities are summed. EMPTY/FULL consider the complete',
        'exposed set. Oversized, broken, or unresolved APIs pause rules instead of returning zero.','',
        'The optional HBM adapter targets inspected 1.0.27_X5808 public APIs: Chemical Plant, Fluid Tank,',
        'Big Ass Tank and supported fluid-enabled multiblock proxies. Its public getAllTanks set is',
        'identified as such; only API-exposed tanks are included. Arc Furnace molten-material lists',
        'are not these fluid tanks. Full assembled structures, dynamic Chemical Factory delegates,',
        'other HBM forks, and individual machines\' redstone behavior require modpack verification.',
        'No direct HE conversion or general machine-control API is added.',
        'See docs/TESTING.md for the actual tested release and remaining coverage.','']
output='\n'.join(lines)
(ROOT/'AUTOMATION_CODES.md').write_text(output,encoding='utf-8')
print('Generated AUTOMATION_CODES.md from authoritative automation_reference.json')
