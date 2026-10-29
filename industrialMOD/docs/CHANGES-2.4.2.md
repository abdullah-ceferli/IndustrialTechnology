# 2.4.2 - Stack empty Advanced Capacitor Banks

Empty banks now stack to 64. Default empty drops share the same item data as crafted banks, and old default empty items normalize on an inventory tick. Configured/named items retain their saved data and stack only with matching items. Placement restores an empty bank's settings while consuming one item from its stack.

Charged banks stay at a stack limit of one so each item retains its own RF. Oversized charged stacks are still rejected by restoration to prevent copying stored energy. No bank capacity, transfer rate, network, texture, recipe or save-format changes. Solar panels still have no GUI.
