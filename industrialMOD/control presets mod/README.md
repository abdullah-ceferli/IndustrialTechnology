# Control Presets — Minecraft 1.7.10

Client-only Forge mod. Open **Options → Controls → Saved Controls**.

Type a name and click **Save current** to save every vanilla/mod binding registered in the Controls screen, including mouse buttons and unbound keys, plus sensitivity, invert mouse and touchscreen. Select any saved preset and click **Load selected** to apply it immediately and save to Minecraft's options. Replacing or deleting a saved preset asks for confirmation.

Presets persist in `config/controlpresets/presets.json`. Copy that file to another installation to transfer presets. Keys are matched by their stable category and description, not their translated label or display order. Keys for absent mods are skipped; newly installed mods retain their current keys. Custom mod controls outside Minecraft's Controls screen are not included. Intentional key conflicts are preserved.

Opening the preset screen for the first time saves **Initial controls**. Before every load, the current layout is saved as **Before last load**; loading that preset undoes the previous load. Each successful file write also keeps `presets.json.bak`. An invalid file displays an error and is kept unchanged; close Minecraft and fix/restore that file before restarting.

Saving/loading happens only when requested in the menu. There are no world ticks, chunk rebuilds, network packets, automatic preset applications or server requirements. Offline Skins is independent and unchanged.

Build using Java 8: `./build.ps1`. Release: `build/libs/ControlPresets-1.7.10-1.0.0.jar`.
