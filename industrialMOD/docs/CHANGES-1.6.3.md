# Industrial Technology 1.6.3

Fixes ambiguous ZIRNOX Blue fuel previews and Auto supply selection.

- Empty assigned positions show dim **P** (planned) rods. Installed rods stay bright.
  Wrong installed fuel has a red **!**. Hover displays actual and assigned fuel names.
  Reactor tooltips wrap and stay inside the visible terminal area.
- The reactor panel shows feeder state, unsaved patterns, the chosen source name
  and installed rod count. The source tooltip includes exact source/access positions,
  NO ADJACENT SUPPLY, NO MATCHING FUEL and setup guidance.
- Supply Auto prefers permitted adjacent stock containing extractable fuel needed
  by an empty assignment. A preceding empty or unrelated inventory no longer hides
  the useful source. Specific direction selection remains exact.
- All 24 native fuel positions still fit one screen. Existing saves, native HBM
  slot IDs, source restrictions, ownership and transfer budgets are retained.

This release does not intercept other mods' direct insertions into HBM. Pipes must
feed the supply chest directly beside the Blue Stick's linked reactor access block.
The chest must contain each assigned fuel type. Remove pre-existing wrong fuel in
the native reactor GUI before expecting that position to fill. Occupied core fuel
is never deleted, replaced or rearranged by the feeder.

Native HBM tests verify Auto source selection, explicit direction and protection.
The actual HBM/NEI client compares every position against HBM's own GUI, including
a seven-rod pipe-style input fixture and a complete 24-rod uranium/thorium pattern
filled by real scheduled feeder ticks with item/NBT conservation. See TESTING.md.
