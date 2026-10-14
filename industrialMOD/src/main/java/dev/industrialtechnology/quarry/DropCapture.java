package dev.industrialtechnology.quarry;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import java.util.List;
import net.minecraft.item.ItemStack;

/** Capture synchronous, real harvest drops (including breakBlock TileEntity inventory drops). */
public final class DropCapture {
    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<Scope>();
    private static final class Scope {
        final TileQuarry quarry; final List<ItemStack> drops;
        Scope(TileQuarry quarry, List<ItemStack> drops) { this.quarry = quarry; this.drops = drops; }
    }
    public static void begin(TileQuarry quarry, List<ItemStack> drops) {
        if (CURRENT.get() != null) throw new IllegalStateException("Nested quarry harvesting");
        CURRENT.set(new Scope(quarry, drops));
    }
    public static void end() { CURRENT.remove(); }
    @SubscribeEvent(priority = EventPriority.LOWEST) public void join(EntityJoinWorldEvent event) {
        Scope scope = CURRENT.get();
        if (scope == null || event.world != scope.quarry.getWorldObj()) return;
        // This scope lives only for a synchronous harvest; unrelated world ticks cannot run inside it.
        if (event.entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) event.entity).getEntityItem();
            if (stack != null && stack.getItem() != null && stack.stackSize > 0) scope.drops.add(stack.copy());
            event.setCanceled(true);
        } else if (event.entity instanceof EntityXPOrb) {
            // Automated quarrying does not farm XP or leave orb entities behind.
            event.setCanceled(true);
        }
    }
}
