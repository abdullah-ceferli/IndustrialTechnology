# Space Shuttle, LC-39 and planetary resources — 2.1.0

See [the player guide](../SPACE_EXPLORATION_GUIDE.md) for survival recipes, placement, operation and HBM pipes. This is part of the existing Industrial Technology Forge 1.7.10 mod.

## Scale and sources

Principal geometry is authored in metres, with one block representing one metre. Whole-block facilities round NASA dimensions; the Shuttle mesh keeps fractional metres. Detailing is a voxel approximation of a Shuttle-era LC-39 facility, not a surveyed reconstruction of every room or pipe.

- [NASA: Space Shuttle](https://www.nasa.gov/reference/the-space-shuttle/): 56.1 m full stack, 37.2 m orbiter length and 23.8 m wingspan. Main engines used LH2/LOX; the real vehicle operated in Earth orbit.
- [NASA: historic launch structures](https://www.nasa.gov/news-release/nasa-explores-new-uses-for-historic-launch-structures-2/): mobile launcher platform dimensions, rounded here to 49 x 41 x 8 m.
- [NASA LC-39 fact sheet](https://www3.nasa.gov/centers/kennedy/pdf/168440main_LC39-06.pdf): 390 x 325 ft hardstand and 347 ft total tower/mast height, rounded to 119 x 99 m and 106 m.
- [NASA countdown guide](https://www3.nasa.gov/centers/kennedy/pdf/146694main_Countdown2003a.pdf): fixed service structure size and levels.

## Geological basis

- [NASA lunar composition](https://science.nasa.gov/moon/composition/): iron/titanium-enriched maria and aluminium/calcium-rich highlands.
- [NASA lunar ilmenite study](https://ntrs.nasa.gov/citations/20080008755): iron/titanium-bearing ilmenite and potential resource use.
- [JPL Martian silica-rich soil](https://www.jpl.nasa.gov/news/mars-rover-investigates-signs-of-steamy-martian-past/): silica-rich material and enhanced titanium.
- [NASA Mars water-ice map](https://science.nasa.gov/resource/a-water-ice-map-for-mars/): shallow regional water ice.
- [NASA comparative soil composition](https://ntrs.nasa.gov/api/citations/20180000883/downloads/20180000883.pdf): constituent Fe/Ti/Al/Si abundance.

Mineral distribution is procedural and bounded to each newly generated chunk. Real mineral chemistry motivates the products; furnace yields and concentrate crafting are simplified game recipes. No previously generated world chunk is replaced or regenerated.

## Engineering limits

The kit places ordinary metadata blocks and one ticking controller. A small world-saved index stores roots, reservations and recovery cursors. Before construction, at most two terrain chunks are prepared per controller tick, including a one-chunk border, so neighbouring world generation completes before parts are placed. Construction then uses checked Forge placement events, a 256-cell controller batch and a 512-cell global per-tick budget. Recovery uses actual built-cell limits, exact block/meta matching and Forge break events. It never sweeps an entire bounding box or deletes arbitrary player blocks. Finite footprint chunk access is used; no permanent chunk-loading tickets are created for the structure.

Native HBM MK2 receiver methods live behind Forge optional-interface stripping; integration is verified against the user's X5771 JAR. The native API returns unaccepted fluid, which the receiver preserves correctly. Pipe connections check typed, loaded real nodes and interaction permissions. The Forge tanks remain the single authoritative stored amounts.

Large Shuttle landings search at most the configured number of candidates (default 49) within +/-96 m of the destination search centre. They require a solid, sufficiently level footprint and a full clear ascent volume; no floating platform or terrain flattening is generated. The legacy rocket retains its smaller landing search.

The complete stack travels and touches down vertically using the existing mission system. Kerosene/LOX, planetary missions and return-stack reuse are intentional gameplay extensions. Historical staging, full Shuttle flight dynamics and runway recovery are outside this change.
