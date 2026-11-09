# Green Stick battery monitoring — 2.8.0

1. Carry your Wireless Terminal and put a **Green stick directly on an HBM Energy Storage Block**.
2. Open the terminal, select **Monitors**, and select that Green stick. Its **Energy** page opens automatically.
3. You see stored HE / maximum HE, a charge bar, **green incoming HE/t**, and **red outgoing HE/t**.
4. To combine batteries, put them **touching in a straight row**, put the Green stick on a middle battery, then click **Batteries: Single**. The button changes to **Batteries: Row (X/Z/Y)**.
5. The energy page now totals that row's stored energy, capacity, incoming rate, outgoing rate, and number of batteries. Click the same button again to return to a single battery.

Two, three, five, or more batteries work. The two middle batteries in an even-length row both work. Different tiers of HBM's original Energy Storage Blocks can share a row. X, Z, and vertical Y rows are supported; the longest straight row through the attached battery is chosen automatically (X, then Z, then Y for ties). Branches are not added.

Keep batteries touching. Gaps, cables, unrelated machines, a different battery implementation, and unloaded chunks stop the row. Discovery checks at most 32 blocks on each side (65 batteries total). An incomplete scan is marked as partial. No chunks are loaded by this monitor. The center battery and the Green stick must be loaded, still present, linked to your own terminal, and in your dimension.

The row selection is saved on the Green endpoint. It survives closing/reopening the terminal and normal world saves. A replacement or broken target cannot reuse a stale energy reading.

## What the rates mean

For the installed HBM X5771 Energy Storage Blocks, input and output are measured independently from actual battery energy writes, including HBM's normal cable transfers and charging/discharging items in the battery slots. These are actual observed transfers, not configured transfer limits or guesses from the net balance. The display averages over up to the last 20 completed game ticks. It briefly says Measuring when observation starts and collects samples while you view the battery in a terminal. It does not record offline history or add a background polling task.

Row input/output totals **sum the individual batteries' transfers**. A transfer between two members therefore appears in both total input and total output; the monitor does not claim to measure only the row's external boundary. Hover the energy area for the exact raw values, averaging note, and net stored-energy change.

Our Advanced Capacitor Bank already exposes its entire connected multiblock as one RF network. Its Green page uses that network once, including real input/output from the bank ledger's last completed tick. It does not multiply an existing network by its member count. RF stays RF and HE stays HE.

Other RF/HBM energy handlers can display the storage values exposed by their APIs. Separate input/output is marked unavailable if the implementation has no supported transfer meter; net change is identified separately in the tooltip. No transfer limits are presented as live transfer rates. The new exact HBM observers target the installed legacy Energy Storage Block class and normal Battery Socket charge setter; other HBM implementations are not rewritten. The REDD's arbitrary-precision storage is not newly supported by this patch's 64-bit reader.

Items and Tanks remain available as separate pages. This update changes no battery recipes, capacities, redstone modes, transfer behavior, HBM JAR files, world terrain, or textures.
