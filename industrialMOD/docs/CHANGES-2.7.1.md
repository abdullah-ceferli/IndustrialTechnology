# 2.7.1 — optional original HBM Spark crafting chain

Added an opt-in compatibility setting for the exact original Spark battery/6/25/100/1000-cell crafting grids, needed to make the legacy Spark Energy Storage Block obtainable in survival. HBM removed four of those cell items; they are restored after existing item registrations, with original names, capacities and unchanged original HBM textures. All crafted batteries/cells start empty. Modern recipes remain present. No HBM dependency when absent and no new ticking work.

The four original storage-block assembler recipes are delivered separately through the existing HBM JSON recipe config, preserving all current template entries. Ordinary NEI crafting handlers show the grids; HBM's own NEI assembler handler shows the block recipes. See LEGACY_HBM_STORAGE.md for exact recipes, source commit, config, deployment/backup information and limits.
