# 2.4.0 — Solar and Railway Expansion

Adds five daylight/weather/sky-aware photovoltaic tiers, cached horizontal arrays, shared legacy RF output and panel diagnostics. Uses credited newest-inspected Ender IO 26.3 cell assets with five original tier frames.

Adds standalone 1.435 m-gauge 16 m railway segments, quarter curves, slopes, switches and redstone stop tracks. Six persistent rolling-stock variants include four locomotives (coal steam, industrial RF electric, ACS-inspired passenger and two-section AZ8A-inspired freight), a three-bin container wagon and a passenger coach. Server physics accounts for mass, grade, traction, resistance and braking, with bounded route/chunk look-ahead. Couplings persist by UUID; engines restore parked.

Steam stores 300 logical coal and 30,000 mB water, warms up and consumes real resources. Electric vehicles use onboard RF, aggregate charge limits and charging stations. Freight bins store 100 ordinary items / 300 coal, with valid normal-stack transfers, saved metadata/NBT and configurable mass. Loader/unloader stations bridge ordinary inventories and logical cargo. Original OBJ models, separate bogie/wheel/rod animation, cabin rendering, headlights, synthesized audio, control GUI and configurable keys are included.

No required rail mod or overhead network. Custom tracks do not claim compatibility with other mods' rolling stock. No reverse-throttle or derailment simulation. Full guide and asset provenance are at the project root.

Appends registries and packet IDs 9/10, GUI IDs 3/4/5. Existing machinery and space-removal behavior remain. Testing uses disposable project worlds; installed saves are untouched.
