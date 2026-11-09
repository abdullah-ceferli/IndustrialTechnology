# 2.7.2 — remove LEGACY labels from restored craftable storage items

Removed the parenthesized LEGACY label from the four restored Energy Storage Block names and four Spark prerequisite cell names. Their actual registry IDs, recipes, item properties, capacities, textures, saved data and HBM integration are unchanged.

A small client resource-reload listener removes the literal LEGACY suffix only from those eight translated item names after normal language loading. It preserves the selected language and leaves unrelated retired items alone. No HBM JAR, texture, language file or installed recipe config is edited.

The existing NEI client inspection records the resulting displayed names. Package comparison verifies all gameplay runtime classes and all old assets remain unchanged. Sources and user worlds are preserved.
