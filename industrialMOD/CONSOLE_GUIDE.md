# Tank History Monitor and Refinery Controller — 2.6.0

Both blocks use your existing Wireless Terminal network. They cost no RF or HE to operate. Sensors, outputs and the console must be loaded in the same dimension. They never load chunks themselves.

## Start here

1. Carry your own Wireless Terminal when placing Green, Blue or Yellow sticks, so they link to it. Put the stick directly on the tank or battery you want to read.
2. Place the new block. Right-click it while holding that terminal to link it.
3. Right-click with an empty hand to open its screen. Use the channel list to select one of 16 channels.

Crafting grids use the existing circuit item. Both recipes appear in NEI.

Tank History Monitor:

```
Iron ingot | Glass       | Iron ingot
Circuit    | Green stick | Circuit
Iron ingot | Redstone    | Iron ingot
```

Refinery Controller:

```
Iron block | Blue stick | Iron block
Circuit    | Ender pearl| Circuit
Iron block | Yellow stick | Iron block
```

## Tank and battery history

The Tank History Monitor initially selects up to 16 linked Green sensors. Click the sensor-name button to choose a different Green stick, or a Yellow stick on a battery. Rename sticks in the terminal to make the list easier to use.

The Fluid/Energy button chooses what to read. Energy is detected automatically when a newly selected sensor exposes energy but no tank. Use Tank to cycle a machine's exposed fluid tanks.

* Fluid amount and capacity are in mB, including HBM Big-Ass Tanks.
* HBM batteries report HE through their actual HBM energy interface.
* Advanced Capacitor Banks report the whole connected network in RF, including totals above 2 billion RF.
* Compatible RF receivers can also report RF. HE and RF are never converted or transferred by monitoring.
* Yellow sticks retain their ordinary redstone controls. The terminal's Controls screen additionally shows energy when the attached machine supports it.

The graph offers 5 minutes, 15 minutes and 1 hour. Sampling is approximately every 5 seconds for a small number of consoles, with bounded shared work across all loaded consoles. Each channel retains at most 720 samples. Heavy console use can slow sampling.

Net rate means the change in stored amount per second; it cannot separately measure simultaneous incoming and outgoing flow. Enter an exact whole-number Target and press Set for an estimate of time to that amount. Estimates use server ticks and require a recent consistent trend. A stopped or reversing trend may have no estimate. Compact M/G labels mean million/billion; input fields accept full integers.

Unavailable sensors create gaps, never false zero readings. Unloaded time is not fabricated. Changing source, tank, fluid identity or energy unit starts a separate series. History survives saving and restarting the world. Picking up the block keeps its links and settings but resets its graph history.

## Refinery Controller

The controller works with tanks supplied by any factory. Each of its 16 rules reads one Blue sensor and controls one Yellow output. No code is required.

Example: release naphtha surplus at 2,000,000 mB, stop releasing at 1,800,000 mB:

1. Place a linked Blue stick on the naphtha tank.
2. Place a linked Yellow stick where redstone should enable the outlet, servo or machine. Configure that device to respond to redstone.
3. In the controller, choose the Blue sensor and Yellow output.
4. Select Fluid and the correct exposed tank, then Release surplus and Automatic.
5. Enter Low `1800000`, High `2000000`; press Apply limits, then Enable.

Release surplus turns ON at or above High, OFF at or below Low, and holds its previous state between them. Refill reverses that: ON at Low, OFF at High. Limits are inclusive and must satisfy `0 <= Low < High <= 1000000000000`. Energy mode applies the same rules to a Blue sensor on a battery, using its native HE or RF unit.

Automatic / Force ON / Force OFF provides a manual override. Enable still applies; missing data still turns an owned output OFF. Pause stops the selected rule; Pause all stops every rule on that controller. Changes to source, tank or mode pause the rule so you can review it before enabling.

Pause an existing Blue program targeting that Yellow before enabling the controller rule. The controller refuses to compete with a running Blue program or another controller rule. A disconnected/disabled/unavailable sensor or output fails closed. A changed fluid type also turns the output OFF until you reselect the sensor. An empty tank reading is valid zero.

Breaking or unloading the controller turns its owned outputs OFF. Settings remain on the dropped block, and rules start paused after placement. World saves preserve enabled rules and limits; normal loaded polling resumes after restart. The block itself has no item/fluid ports: Blue reads, Yellow supplies redstone, and your existing pipes move materials.

## Appearance and performance

The cases use the game's iron texture and simple pixel screen geometry: teal graph for history, amber control indicators for the controller. The GUI is drawn in code. There are no AI-generated textures, added texture files, continuously animated block screens or tile-entity renderers.

Only the selected graph is sent to an open screen. Polling is bounded globally, and neither block creates chunk tickets or scans the world. Existing player saves and installed Minecraft files were not changed during development.
