package dev.industrialtechnology.client;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.core.IndustrialTechnology;
/** MouseEvent runs before vanilla processes the attack/break mouse binding. */
public final class WirelessInput {
    @SubscribeEvent public void mouse(MouseEvent event){
        Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen!=null||!ItemWirelessTerminal.held(mc.thePlayer)||event.button!=0)return;
        event.setCanceled(true);KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(),false);
        if(event.buttonstate)IndustrialTechnology.network.sendToServer(new WirelessCommand(0,WirelessCommand.OPEN,0,0,""));
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.START)return;Minecraft mc=Minecraft.getMinecraft();
        if(ItemWirelessTerminal.held(mc.thePlayer))KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(),false);
    }
}
