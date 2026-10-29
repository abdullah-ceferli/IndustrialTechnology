# Industrial Technology 2.3.1 — Correct default capacitor appearance

2.3.0 mistakenly painted the optional charge-display atlas across the bank's default front face.
2.3.1 restores the normal plain Ender IO Vibrant casing. No charge overlay is forced on newly placed
or upgraded banks, or on the inventory item. The seven original texture PNGs remain unchanged.

Sneak + right-click an individual Both face with an empty hand to toggle its optional meter;
normal right-click still opens the bank GUI. The optional meter reads only the SINGLE quarter of
Ender IO's four-part atlas, using the original 0.25-width, 0.8-height proportions.
Display choices persist on the tile and charged dropped item and synchronize to clients.
Hidden meters do not trigger charge-level redraw packets.

The Ender IO project wiki documents that exterior gauges/screens are optional side displays:
https://github.com/SleepyTrousers/EnderIO-1.5-1.12/wiki/Capacitor-Banks
The installed Ender IO 2.3.0.430_beta's renderer and FillGauge atlas mapping were also inspected.

Capacity, RF ledgers, network membership, budgets, GUI, recipes and other machines are unchanged.
No installed mod or human user's world is edited by this build.
