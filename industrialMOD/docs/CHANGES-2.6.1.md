# 2.6.1 — stable monitor screens in every direction

Fixed flickering/striped screen details when a Tank History Monitor or Refinery Controller faces sideways. The previous frame, screen background and glyphs used overlapping surfaces only 0.0001–0.0003 blocks apart. Depth precision could make those layers fight, particularly when seen at an angle.

Both displays now use a single pixel surface with no overlapping quads, set 1/64 block in front of the iron casing. Binary-exact coordinates and common outward-facing geometry work in all four horizontal directions. Adjacent same-color pixels merge into cached rectangles when the class loads, keeping the mesh small. The palette and simple industrial style remain; no texture files or AI-generated artwork are added.

The renderer also safely falls back to the south-facing display for invalid facing metadata. This is a rendering patch: monitor/controller rules, wireless links, saved history, all other machines and every asset file remain unchanged from 2.6.0. Existing blocks are fixed by replacing the JAR and restarting the client; no breaking/replacing blocks or editing saves is required.

See docs/TESTING.md for executed geometry, four-direction graphical and production-loading checks.
