package dev.industrialtechnology.wireless;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
public final class TerminalSafety {
    @SubscribeEvent public void attack(AttackEntityEvent e){if(ItemWirelessTerminal.held(e.entityPlayer)){e.setCanceled(true);ItemWirelessTerminal.open(e.entityPlayer);}}
    @SubscribeEvent public void interact(PlayerInteractEvent e){if(e.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK&&ItemWirelessTerminal.held(e.entityPlayer)){e.setCanceled(true);ItemWirelessTerminal.open(e.entityPlayer);}}
}
