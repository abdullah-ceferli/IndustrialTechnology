# 2.6.0 — tank history and factory control consoles

Added Tank History Monitor and Refinery Controller, each with 16 owner-bound wireless channels, survival recipes and native industrial pixel displays. Monitor Green-linked fluid tanks, HBM HE batteries or Advanced Capacitor Bank RF networks. Yellow sticks can report energy in both the new monitor and the existing terminal Controls page while retaining their redstone controls.

History includes bounded saved samples, 5/15/60-minute graphs, net stored change and a target-time estimate. Unavailable data creates gaps. Controllers provide release/refill Low/High rules, manual override, individual/all pause, fail-closed unavailable handling, and exclusive Yellow ownership with checks against existing Blue programs. Any factory can supply a monitored tank.

New common GUI ID 7 and packet IDs 11/12 follow existing registrations. Packets validate player ownership, loaded tile, range, window and rate on the server thread. No new optional dependency is required. Existing machine registrations remain ordered, solar panels remain without a GUI, and space content remains removed.

No generated textures or new raster assets. No user world or installed game files edited. See CONSOLE_GUIDE.md for setup and limits; docs/TESTING.md records executed verification.
