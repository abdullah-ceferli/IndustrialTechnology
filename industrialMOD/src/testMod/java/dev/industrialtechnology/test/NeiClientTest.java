package dev.industrialtechnology.test;
import java.io.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.world.*;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class NeiClientTest {
    private final List<String> results=new ArrayList<String>();private int phase,wait=30,startup;private boolean finished;
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||finished||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();try{
        if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;WorldSettings settings=new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);settings.func_82750_a("2;7;1;");mc.launchIntegratedServer("nei-render","NEI Hover Regression",settings);phase=1;wait=80;return;}
        if(mc.thePlayer==null||mc.theWorld==null){if(++startup>60)throw new AssertionError("NEI join timeout");wait=20;return;}
        mc.displayGuiScreen(null);TooltipClientChecks.run(mc,message->{results.add("PASS: "+message);IndustrialTechnology.log.info("NEI CLIENT PASS: "+message);});finish(mc,false);
    }catch(Throwable error){results.add("FAIL: "+error);IndustrialTechnology.log.error("NEI CLIENT FAILED",error);finish(mc,true);}}
    private void finish(Minecraft mc,boolean failed){finished=true;try{results.add("Failed: "+(failed?1:0));Files.write(new File(mc.mcDataDir,"nei-client-integration.txt").toPath(),results,StandardCharsets.UTF_8);}catch(IOException error){IndustrialTechnology.log.error("NEI report failed",error);}mc.shutdown();}
}
