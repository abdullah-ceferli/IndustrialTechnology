# Industrial Technology 1.6.2

The Wireless Terminal no longer draws the “4 / 4 devices” style count below
its linked-device list. Search and device-list page navigation still work.

ZIRNOX Grid now displays every native fuel position together: all 24 positions
in seven staggered rows. Native slot IDs, ordering and item icons are preserved.
Horizontal gaps are narrower; icons stay 16×16. Grid page arrows and its page
counter are removed. The optional fuel-name List keeps its existing pages.

The full grid uses the lower part of the reactor panel. In Blue automation,
click Inventory beside Supply to open your real player inventory in that space.
Picking up a rod returns to the grid automatically; click the assignment ghost
as usual. Open Inventory again to put the real cursor item away, then Fuel grid.
Escape closes the inventory drawer first. The server's inventory indices,
locked terminal slot, cursor ownership and ghost validation remain unchanged.

The terminal stays at its existing size and GUI scale. Reactor rows, inventory
slots, side details and NEI controls are kept separate. Other terminal views
restore their normal inventory layout. Quarry settings retention from 1.6.1,
reactor data/control and all saved programs remain included.

Changed files: GuiWirelessTerminal.java, GuiReactorPanel.java, ReactorClientTest.java,
the shared guide JSON/generated reference, README, REACTORS.md, build version,
release audit and versioned test evidence. See TESTING.md for performed checks.
