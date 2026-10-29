# Industrial Technology 2.3.0 — Advanced Capacitor Bank

Adds a six-face RF multiblock battery. Every bank adds 25,000,000 RF and a shared
25,000 RF/t input plus 25,000 RF/t output budget. Rows, towers, walls, cubes and irregular
connected structures share storage automatically. Only loaded banks contribute.

RF remains owned by individual saved bank cells. Removing a bridge creates independent
components; normal pickaxe harvesting saves the removed cell's RF and settings in its item.
Simulation calls never spend RF. Same-tick quotas survive component reconstruction.

The GUI provides actual network statistics, shared rate limits, Auto/Max controls, output
redstone modes and a six-face configuration diagram. Each bank face has Input, Output,
Both and Disabled modes. RF is pushed to compatible receivers using the legacy CoFH API.

The user's later request replaces the initial original block artwork with seven unchanged
textures from their installed Ender IO 2.3.0.430_beta. Credits, the source LICENSE and NOTICE
are included. The chunk-mesh renderer composes those original layers with connected borders;
it does not run a TileEntity renderer. Gauge updates are quantized and rate limited.

Existing machine/item IDs and all prior machines are retained. Space content remains removed.
This update does not install itself into the user's game or modify existing saved worlds.

See [CAPACITOR_BANK.md](CAPACITOR_BANK.md) for setup, crafting and configuration.
