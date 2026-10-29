package dev.industrialtechnology.test;

import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.console.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ScreenShotHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;

/** Native chunk-mesh rendering from both close and oblique views in all horizontal directions. */
public final class ConsoleFacingClientChecks {
    private final List<String> report=Collections.synchronizedList(new ArrayList<String>());
    private int phase,view,wait=30,age;
    private volatile boolean ready,failed;
    private boolean done;
    private static final int[] FACES={2,3,4,5};
    private static final String[] NAMES={"north","south","west","east"};
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    private void setupView(final int index) {
        ready=false;
        ServerWork.enqueue(()->{
            try {
                EntityPlayerMP p=(EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);
                WorldServer w=(WorldServer)p.worldObj;
                int f=FACES[index/2];
                w.setWorldTime(6000);w.getGameRules().setOrCreateGameRule("doDaylightCycle","false");
                p.capabilities.allowFlying=true;p.capabilities.isFlying=true;p.sendPlayerAbilities();
                for(int x=12;x<=29;x++)for(int z=12;z<=29;z++)w.setBlock(x,71,z,Blocks.stone,0,3);
                for(int[] xz:new int[][]{{20,20},{22,20},{20,22}})w.setBlockToAir(xz[0],72,xz[1]);
                p.rotationYaw=f==2?0:f==3?180:f==4?270:90;
                int cx=f<4?22:20,cz=f<4?20:22;
                w.setBlock(20,72,20,ConsoleRegistry.history,0,3);
                ConsoleRegistry.history.onBlockPlacedBy(w,20,72,20,p,new ItemStack(ConsoleRegistry.history));
                w.setBlock(cx,72,cz,ConsoleRegistry.controller,0,3);
                ConsoleRegistry.controller.onBlockPlacedBy(w,cx,72,cz,p,new ItemStack(ConsoleRegistry.controller));
                check(w.getBlockMetadata(20,72,20)==f && w.getBlockMetadata(cx,72,cz)==f,"placement facing");
                double tx=f<4?21.5:20.5,tz=f<4?20.5:21.5,dist=index%2==0?4:12;
                double x=tx+(f==4?-dist:f==5?dist:index%2==0?0:3),z=tz+(f==2?-dist:f==3?dist:index%2==0?0:3);
                double dx=tx-x,dz=tz-z;
                float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
                float pitch=(float)Math.toDegrees(Math.atan2(.12,Math.sqrt(dx*dx+dz*dz)));
                p.playerNetServerHandler.setPlayerLocation(x,71,z,yaw,pitch);ready=true;
            } catch(Throwable t){failed=true;report.add("FAIL: setup :: "+t);IndustrialTechnology.log.error("Facing fixture",t);}
        });
    }
    private static int[] accents(Minecraft mc) {
        int w=mc.displayWidth,h=mc.displayHeight,teal=0,amber=0;
        ByteBuffer data=BufferUtils.createByteBuffer(w*h*3);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT,1);
        GL11.glReadPixels(0,0,w,h,GL11.GL_RGB,GL11.GL_UNSIGNED_BYTE,data);
        for(int y=h/4;y<h*3/4;y++)for(int x=w/5;x<w*4/5;x++) {
            int i=(y*w+x)*3,r=data.get(i)&255,g=data.get(i+1)&255,b=data.get(i+2)&255;
            if(g>r+15 && b>r+10 && g>=b && r>20)teal++;
            if(r>g*1.1 && g>b*1.3 && r>50)amber++;
        }
        return new int[]{teal,amber};
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
        if(done||e.phase!=TickEvent.Phase.END||--wait>0)return;
        Minecraft mc=Minecraft.getMinecraft();
        try {
            if(failed){finish();return;}
            if(phase==0) {
                if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}
                mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;
                mc.gameSettings.showInventoryAchievementHint=false;
                WorldSettings s=new WorldSettings(261,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);
                s.func_82750_a("2;7;1;");mc.launchIntegratedServer("console-facing","Disposable screen facing QA",s);
                phase=1;wait=70;return;
            }
            if(mc.thePlayer==null||mc.theWorld==null){if(++age>100)throw new AssertionError("join timeout");wait=10;return;}
            if(phase==1) {
                mc.displayGuiScreen(null);TooltipClientChecks.prepare(mc);mc.gameSettings.hideGUI=true;
                setupView(0);phase=2;wait=50;return;
            }
            if(!ready){wait=10;return;}
            int[] count=accents(mc);int min=view%2==0?30:5;
            check(count[0]>=min&&count[1]>=min,"screen details missing: teal="+count[0]+", amber="+count[1]);
            check(GL11.glGetError()==GL11.GL_NO_ERROR,"GL error");
            String name=NAMES[view/2]+(view%2==0?"-near":"-oblique");
            ScreenShotHelper.saveScreenshot(mc.mcDataDir,"console-"+name+".png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());
            report.add("PASS: Actual monitor/controller screen details render "+name+" (teal="+count[0]+", amber="+count[1]+") without GL errors");
            if(++view==8){finish();return;}
            setupView(view);wait=45;
        } catch(Throwable t){failed=true;report.add("FAIL: view "+view+" :: "+t);IndustrialTechnology.log.error("Facing client",t);finish();}
    }
    private void finish() {
        if(done)return;done=true;
        try{report.add("Failed: "+(failed?1:0));Files.write(new File(Minecraft.getMinecraft().mcDataDir,"console-facing-report.txt").toPath(),report,StandardCharsets.UTF_8);}
        catch(Exception e){IndustrialTechnology.log.error("Facing report",e);}
        Minecraft.getMinecraft().shutdown();
    }
}
