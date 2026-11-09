package dev.industrialtechnology.test;
import java.util.*;import java.io.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.lang.reflect.*;
import cpw.mods.fml.common.*;import cpw.mods.fml.common.gameevent.TickEvent;import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.industrialtechnology.client.*;import dev.industrialtechnology.core.*;import dev.industrialtechnology.wireless.*;
import net.minecraft.client.*;import net.minecraft.client.gui.*;import net.minecraft.entity.player.EntityPlayerMP;import net.minecraft.server.MinecraftServer;import net.minecraft.world.*;import net.minecraft.item.*;import net.minecraft.nbt.*;import net.minecraft.util.ScreenShotHelper;import org.lwjgl.opengl.GL11;
public final class BatteryClientChecks {
    private final List<String> report=Collections.synchronizedList(new ArrayList<String>());private int phase,wait=30,age;private volatile boolean ready,failed;private boolean done;private UUID green;
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    private void pass(String s){report.add("PASS: "+s);}
    private static EntityPlayerMP player(){return (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);}
    private static WorldServer world(){return (WorldServer)player().worldObj;}
    private void server(Runnable task){ServerWork.enqueue(()->{try{task.run();}catch(Throwable t){failed=true;report.add("FAIL: server :: "+t);IndustrialTechnology.log.error("Battery visual fixture",t);}});}
    private static void call(Object b,String method,long amount){try{b.getClass().getMethod(method,long.class).invoke(b,amount);}catch(Exception e){throw new RuntimeException(e);}}
    private void setup(){WorldServer w=world();EntityPlayerMP p=player();p.inventory.mainInventory[0]=new ItemStack(ModRegistry.terminal);p.inventory.currentItem=0;WirelessAccess.terminal(p,p.getHeldItem(),true);
        for(int x=20;x<=24;x++){w.setBlock(x,72,24,com.hbm.blocks.ModBlocks.machine_battery,0,3);Object b=w.getTileEntity(x,72,24);try{b.getClass().getField("redLow").setShort(b,(short)3);b.getClass().getField("redHigh").setShort(b,(short)3);}catch(Exception e){throw new RuntimeException(e);}call(b,"setPower",100000);}
        p.playerNetServerHandler.setPlayerLocation(22.5,72,27,180,0);ItemStack s=new ItemStack(ModRegistry.greenStick);((ItemWirelessStick)s.getItem()).onItemUseFirst(s,p,w,22,72,24,3,.5F,.5F,1);
        TileRedstoneRelay relay=(TileRedstoneRelay)w.getTileEntity(22,72,25);check(relay!=null&&relay.endpoint!=null,"Green failed to place");green=relay.endpoint;WirelessData.get(w).devices.get(green).name="HBM battery row";ready=true;open();
    }
    private void open(){player().openGui(IndustrialTechnology.instance,1,world(),0,0,0);TerminalContainer c=(TerminalContainer)player().openContainer;c.selected=green;}
    @SubscribeEvent public void supply(TickEvent.ServerTickEvent e){if(!ready||done||e.phase!=TickEvent.Phase.START)return;try{for(int x=20;x<=24;x++){Object b=world().getTileEntity(x,72,24);call(b,"transferPower",3000);call(b,"usePower",1000);}}catch(Throwable t){failed=true;report.add("FAIL: transfer fixture "+t);}}
    private static GuiButton button(GuiWirelessTerminal g,int id)throws Exception{Field f=GuiScreen.class.getDeclaredField("buttonList");f.setAccessible(true);for(Object o:(List)f.get(g))if(((GuiButton)o).id==id)return (GuiButton)o;throw new AssertionError("missing button "+id);}
    private static void click(GuiWirelessTerminal g,int id)throws Exception{GuiButton b=button(g,id);check(b.visible&&b.enabled,"disabled button "+id);Method m=GuiWirelessTerminal.class.getDeclaredMethod("actionPerformed",GuiButton.class);m.setAccessible(true);m.invoke(g,b);}
    private static void shot(Minecraft mc,String name){try{Method capture=TooltipClientChecks.class.getDeclaredMethod("shot",Minecraft.class,GuiScreen.class,int.class,int.class,String.class);capture.setAccessible(true);capture.invoke(null,mc,mc.currentScreen,0,0,name);}catch(Exception e){throw new RuntimeException(e);}}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(done||e.phase!=TickEvent.Phase.END||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();try{
        if(failed){finish();return;}
        if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;WorldSettings s=new WorldSettings(280,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);s.func_82750_a("2;7;1;");mc.launchIntegratedServer("battery-client","Disposable battery monitor QA",s);phase=1;wait=70;return;}
        if(mc.thePlayer==null||mc.theWorld==null){if(++age>100)throw new AssertionError("join timeout");wait=10;return;}
        if(phase==1){mc.displayGuiScreen(null);TooltipClientChecks.prepare(mc);server(this::setup);phase=2;wait=60;return;}
        check(mc.currentScreen instanceof GuiWirelessTerminal,"terminal missing");GuiWirelessTerminal g=(GuiWirelessTerminal)mc.currentScreen;NBTTagCompound n=g.state.getCompoundTag("Energy");
        if(phase==2){check(n.getBoolean("Available")&&n.getInteger("Count")==1,"single energy packet");check(button(g,28).visible&&((MetalButton)button(g,28)).selected,"Energy tab was not selected automatically");check(n.getLong("Input")==3000&&n.getLong("Output")==1000,"single actual flow "+n);shot(mc,"battery-single.png");pass("Green opens directly to live HBM HE with separate green input and red output");click(g,29);phase=3;wait=50;return;}
        if(phase==3){check(n.getBoolean("Row")&&n.getInteger("Count")==5&&n.getLong("Capacity")==5000000,"row toggle packet "+n);check(n.getLong("Input")==15000&&n.getLong("Output")==5000,"row actual flow "+n);shot(mc,"battery-row-five.png");pass("Actual terminal button combines five center-linked HBM batteries and their live transfer rates");Method capture=TooltipClientChecks.class.getDeclaredMethod("shot",Minecraft.class,GuiScreen.class,int.class,int.class,String.class);capture.setAccessible(true);capture.invoke(null,mc,g,g.panelLeft()+180,g.panelTop()+108,"battery-row-hover.png");server(()->{player().closeScreen();open();});phase=4;wait=35;return;}
        if(phase==4){check(n.getBoolean("Row")&&n.getInteger("Count")==5,"row preference not retained");click(g,18);phase=5;wait=15;return;}
        if(phase==5){shot(mc,"battery-items-existing.png");click(g,19);phase=6;wait=15;return;}
        if(phase==6){shot(mc,"battery-tanks-existing.png");click(g,28);mc.gameSettings.guiScale=2;net.minecraft.client.gui.ScaledResolution r=new net.minecraft.client.gui.ScaledResolution(mc,mc.displayWidth,mc.displayHeight);g.setWorldAndResolution(mc,r.getScaledWidth(),r.getScaledHeight());phase=7;wait=15;return;}
        if(phase==7){check(button(g,29).visible&&button(g,29).enabled,"row button after scale change");shot(mc,"battery-row-scale-two.png");check(GL11.glGetError()==0,"GUI OpenGL error");pass("Row setting survives reopening; Items/Tanks and GUI scale 2 render without GL errors");click(g,29);phase=8;wait=25;return;}
        if(phase==8){check(!n.getBoolean("Row")&&n.getInteger("Count")==1,"single toggle did not restore");pass("Same button restores a single HBM battery through the real server command");finish();}
    }catch(Throwable t){failed=true;report.add("FAIL: client phase "+phase+" :: "+t);IndustrialTechnology.log.error("Battery client",t);finish();}}
    private void finish(){done=true;try{report.add("Failed: "+(failed?1:0));Files.write(new File(Minecraft.getMinecraft().mcDataDir,"battery-client-report.txt").toPath(),report,StandardCharsets.UTF_8);}catch(Exception e){IndustrialTechnology.log.error("Battery client report",e);}Minecraft.getMinecraft().shutdown();}
}
