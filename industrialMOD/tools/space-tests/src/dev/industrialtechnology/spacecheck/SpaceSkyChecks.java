package dev.industrialtechnology.spacecheck;
import java.io.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;import java.util.*;
import cpw.mods.fml.common.FMLCommonHandler;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.*;import net.minecraft.server.MinecraftServer;import net.minecraft.entity.player.EntityPlayerMP;import net.minecraft.world.*;import net.minecraft.util.ScreenShotHelper;import dev.industrialtechnology.core.*;import dev.industrialtechnology.space.*;
public final class SpaceSkyChecks {
    private int step,wait=30;private boolean done;private final List<String> report=new ArrayList<String>();
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    private void check(boolean b,String m){SpaceChecks.check(b,m);report.add("PASS: "+m);}
    private void travel(int dim,int x){ServerWork.enqueue(()->{EntityPlayerMP p=(EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);WorldServer w=RocketTravel.world(dim);MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(p,dim,new RocketTravel.LandingTeleporter(w,x,215,0));p.setGameType(WorldSettings.GameType.CREATIVE);p.capabilities.isFlying=true;p.sendPlayerAbilities();});}
    private BufferedImage shot(String name)throws Exception{Minecraft mc=Minecraft.getMinecraft();ScreenShotHelper.saveScreenshot(mc.mcDataDir,name+".png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());return ImageIO.read(new File(mc.mcDataDir,"screenshots/"+name+".png"));}
    private int earthPixels(BufferedImage im){int count=0;for(int x=im.getWidth()/5;x<im.getWidth()*4/5;x++)for(int y=80;y<im.getHeight()*4/5;y++){int rgb=im.getRGB(x,y),r=rgb>>16&255,g=rgb>>8&255,b=rgb&255;if(b>110&&b>r*1.35&&b>g*1.2)count++;}return count;}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||done||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();try{
        if(step==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=2;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;WorldSettings s=new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);s.func_82750_a("2;7;1;");mc.launchIntegratedServer("space-sky","Space Sky Verification",s);step++;wait=100;return;}
        if(mc.thePlayer==null||mc.theWorld==null){wait=20;return;}
        if(step==1){mc.displayGuiScreen(null);travel(SpaceConfig.moon,0);step++;wait=100;return;}
        if(step==2||step==4){mc.thePlayer.rotationYaw=0;mc.thePlayer.rotationPitch=-60;step++;wait=30;return;}
        if(step==3){check(mc.theWorld.provider.getSkyRenderer() instanceof dev.industrialtechnology.client.space.PlanetSky,"Custom lunar sky renderer is active at three-chunk view distance");check(earthPixels(shot("moon-near-side-earth"))>40,"Recognizable blue Earth is visible above the near-side horizon");travel(SpaceConfig.moon,32768);step++;wait=100;return;}
        if(step==5){check(earthPixels(shot("moon-far-side-no-earth"))<10,"Earth is absent from the lunar far-side sky");travel(SpaceConfig.mars,0);step++;wait=100;return;}
        if(step==6){mc.thePlayer.rotationYaw=0;mc.thePlayer.rotationPitch=-60;step++;wait=30;return;}
        if(step==7){BufferedImage im=shot("mars-dusty-sky");int rgb=im.getRGB(im.getWidth()/2,im.getHeight()/2-80),r=rgb>>16&255,b=rgb&255;check(r>b,"Mars has its own reddish dusty sky");report.add("Failed: 0");finish();}
    }catch(Throwable ex){report.add("FAIL: sky phase "+step+" :: "+ex);report.add("Failed: 1");try{shot("sky-error");}catch(Exception ignored){}finish();}}
    private void finish(){done=true;try{Files.write(new File(Minecraft.getMinecraft().mcDataDir,"space-sky-report.txt").toPath(),String.join("\n",report).getBytes(StandardCharsets.UTF_8));}catch(IOException e){IndustrialTechnology.log.error("Sky report",e);}Minecraft.getMinecraft().shutdown();}
}
