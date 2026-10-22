# Industrial Technology 1.6.0

Green links now inspect RBMK fuel depletion, remaining fuel and core/skin °C,
and ZIRNOX port links inspect all native fuel positions plus reactor °C.
Blue adds an additional ZIRNOX assignment editor with real adjacent-inventory
fuel loading, and an RBMK console insertion slider with native rod motion.
Every terminal tab has responsive local search with clear and filtered pages.

The HBM adapter is isolated and optional, based on the installed X5808 classes.
There is no separate C depletion statistic in that version; fuel depletion is
shown as a percentage independently from Celsius temperature.

## Implementation

- HbmReactors: verified controller/proxy resolution, identity binding, native
  fuel calculations, common-container layout, connected manual-rod control group.
- ReactorSettings, ReactorAutomation, ReactorPermissions and ReactorTerminal:
  saved assignments, bounded local transfers with NBT preservation, owner
  protection checks and authoritative window-scoped drafts/requests.
- GuiReactorPanel and GuiWirelessTerminal: grid/list/detail views, ghost editor,
  insertion controls, independent local searches and scoped item rendering.
- DeviceSearch, WirelessCatalog and CatalogAssembler: local matching and bounded
  complete catalog generations; CommonProxy/ClientProxy handle sided delivery.
- WirelessEndpoint/Config/Scheduler/TerminalContainer: backward-compatible fields,
  feeder configuration/polling and integration with existing SIMPLE/CODE modes.
- build.gradle/IndustrialTechnology: 1.6.0, no new runtime dependency.
- Shared guide JSON, generated AUTOMATION_CODES.md, README and REACTORS.md:
  linking, supported data meanings, physical supply placement, controls and limits.
- Unit tests and native server/client fixtures cover the added behaviors.

Quarry RF balance, generic ore harvesting, VOID filters, chunk tickets, ordinary
sticks, SIMPLE/CODE programs, EVERY timing and existing tooltip lighting are retained.
See TESTING.md for evidence, actual coverage and reproduction commands.
