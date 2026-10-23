# Industrial Technology 2.2.0

The space expansion is removed: rockets/Shuttle, launch facilities, Moon/Mars dimensions and terrain, research, space machines, suits, oxygen/pressure systems, planetary minerals, their recipes/fluids, and related UI/art. Native HBM remains optional for the original reactor monitoring/automation features; the space-only HBM fluid-pipe adapter is removed.

The **Green Redstone Lamp stays**, under its original `industrialtech:green_redstone_lamp` registry name and with its original textures and recipe. Attach a vanilla lever to its top, bottom or any side. Switching it on emits light level 15; switching it off emits 0. Attachment faces remain solid in both states.

Crafting recipe (four lamps): glass / glass / glass; green dye / glowstone / green dye; glass / redstone / glass. Quarries, sparse chunk loading, wireless sticks/terminal, SIMPLE/CODE automation, quarry item settings/VOID filters and HBM reactor control retain their pre-space behavior. The space-added empty-dimension suspension policy is removed with the expansion; original configured chunk loading works without online players again.

## Existing saves

Back up a world before installing a version that removes content. Once a save is opened and saved under 2.2.0, **removed space blocks, items, entities and their stored contents are lost**. The mod accepts only the exact known retired `industrialtech` block/item names; unrelated missing-mod mappings retain Forge's standard handling. Existing green lamps and all retained machinery keep their names.

World folders, old planetary region files and old configuration files are not deleted. Moon/Mars providers are no longer registered. A targeted login migration returns players saved on the removed Moon/Mars to the Overworld before the client receives its login packet, preserving their non-space inventory and XP. It reads the old configured planet IDs if `industrialtech-space.cfg` exists and leaves dimensions currently registered by another mod alone. Returning to Earth and storing non-space items before upgrading is preferable when possible. Old planetary files remain unused and can be restored together with a backup and the earlier mod version.

No installed Minecraft world or mod JAR was edited during development. All validation uses disposable test directories or copies of earlier disposable test saves. Replace the old IndustrialTechnology JAR with `IndustrialTechnology-1.7.10-2.2.0.jar` on both client and server; keep the original config and save folders.
