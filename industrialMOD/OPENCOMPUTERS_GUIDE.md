# Complete OpenComputers in Industrial Technology 2.9.0

This release contains the entire original OpenComputers **1.8.10 for Minecraft 1.7.10**, upstream commit `667626d`, in the Industrial Technology runtime JAR. Its original mod ID, registry names, code, textures, models, sounds, manual, recipes, programs and native libraries are preserved. This is the real programmable computer mod, with no online AI service.

## Install and find it

Use Forge 10.13.4.1614 and Java 8. Replace the previous Industrial Technology JAR with `IndustrialTechnology-1.7.10-2.9.0.jar` on the client and server/LAN participants. Keep only one Industrial Technology version. **Do not also install a separate OpenComputers JAR:** the combined file already supplies the `OpenComputers` mod ID. Two entries in Forge's mod list are expected: Industrial Technology and OpenComputers.

Open the **OpenComputers** creative tab, or search NEI for Computer Case, Screen, Keyboard, OpenOS, or the OpenComputers manual. The original survival recipes remain available through NEI. The complete standalone default catalog has 169 named descriptors and 192 registered crafting recipes; optional integrations and configuration can affect recipes in a larger pack.

## What is included

- All computer cases and tiers, CPUs/APUs, RAM, GPUs, hard disks, floppy disks, EEPROMs, cards and upgrades.
- OpenOS and the original Lua firmware/program disks, including the original package tools and bundled alternate systems.
- Screens, keyboards, cables, network/Internet/wireless/linked cards, adapters, redstone interfaces and transposers.
- Robots, drones, tablets, microcontrollers, their assembly/disassembly systems and upgrades.
- Server racks/modules, terminals, switches, access points, chargers, printers and hologram projectors.
- The original in-game manual, original art/audio, native Lua architectures and Java Lua fallback.
- All upstream external-mod integration code. Each integration still requires its corresponding external mod/version; bundling does not add AE2, HBM or other external mods themselves.

The upstream archive and exact source archive are pinned in `vendor/opencomputers/upstream.json`. Its original notices are preserved. The source distribution includes the original Scala/Java sources and original resources under `opencomputers/`.

## First computer

1. Place a Computer Case, Screen and Keyboard touching as described in the original manual. A cable can connect the case and screen. Place the keyboard against the screen or beside it with a valid connection.
2. Right-click the case and install a compatible CPU, RAM, graphics card, hard drive, **Lua BIOS EEPROM**, and **OpenOS floppy**. A Tier 3 case has a floppy slot; a Disk Drive can hold the floppy externally for other cases.
3. Supply power through the original supported energy connections. In Creative, use the Creative Case for testing.
4. Press the case power button. Right-click the screen to use the original OpenOS terminal.
5. Run `install`, choose the hard drive, follow OpenOS's prompts, remove the floppy when instructed and reboot.

The original manual explains slot tiers, component limits, memory and hardware compatibility. Original documentation: https://ocdoc.cil.li/ . Original release: https://github.com/MightyPirates/OpenComputers/releases/tag/1.7.10-forge/1.8.10 .

## Reading our factory blocks

An original OC **Adapter** beside an Advanced Capacitor Bank, Tank History Monitor, Refinery Controller or a valid owned Green monitoring stick exposes a component named `industrialtech`. Connect the Adapter to the computer with OC cables. Multiple adapters have distinct addresses; use `component.list("industrialtech")` to enumerate them.

The added driver is read-only and runs callbacks on the server thread. It creates no tick listener, chunk loader, wireless control service or separate polling thread. Green sticks must retain their valid physical owner/network pairing. Unloaded or replaced targets become unavailable.

```lua
local component = require("component")
local event = require("event")
local address = component.list("industrialtech")()
assert(address, "Connect an Adapter beside a supported factory block")
local factory = component.proxy(address)
while true do
  local energy = factory.getEnergy()
  if energy.available then
    print(energy.storedExact .. " / " .. energy.capacityExact .. " " .. energy.unit)
    if energy.ratesAvailable then
      print("in " .. energy.inputExact .. "/t, out " .. energy.outputExact .. "/t")
    end
  end
  event.pull(1) -- yield between readings
end
```

Callbacks:

| Method | Result |
| --- | --- |
| `getInfo()` | Kind and loaded availability. |
| `getEnergy()` | Storage/capacity, real supported input/output rates, unit, battery count, row/partial flags and rate availability. Capacitor networks are counted once. |
| `getTanks()` | Supported tanks exposed by a linked Green stick: fluid name/id, amount, capacity and unit. |
| `getChannels()` | A monitor/controller's sixteen cached channels, with name, enabled/output flags, status and available sensor values. |

Energy numbers also have decimal-string `storedExact`, `capacityExact`, `inputExact`, `outputExact` fields. Use those for exact display when values exceed Lua's numeric precision. `available=false` or `ratesAvailable=false` means no supported reading, not an inferred zero. Existing Green row settings are used; this driver does not change them. A Green stick must be physically accessible to the Adapter.

## Performance and configuration

Original OC execution budgets, power costs and limits remain at upstream defaults. Its normal configuration is generated as `config/OpenComputers.cfg` on first launch. Computer scripts, active robots and large animated screens consume resources; adding the mod alone does not guarantee a particular FPS. Programs should yield/sleep and sample factory data at sensible intervals, such as once a second, rather than busy-looping. This release does not change your existing Industrial Technology or HBM recipe configuration.

The complete content is included; no promise is made that every possible program or external-mod combination has been individually exercised. `docs/test-results/2.9.0` records the actual native, restart, stock-comparison and graphical checks performed.
