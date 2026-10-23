package dev.industrialtechnology.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.industrialtechnology.client.GuiMachine;
import dev.industrialtechnology.client.GuiChunkLoader;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.core.ModRegistry;
import dev.industrialtechnology.core.ServerWork;
import dev.industrialtechnology.machine.TileMachine;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.quarry.ChunkArea;
import dev.industrialtechnology.network.ControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.util.ScreenShotHelper;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

/** Exercises actual GUIs, SimpleImpl messages, vanilla inventory packets, and OpenGL rendering. */
public final class ClientTestProxy extends TestProxy {
    private final List<String> results=Collections.synchronizedList(new ArrayList<String>());
    private int phase,tier,wait=30,step,diamondsBefore,filterSourceSlot,carriedBefore,filterDiamondTotal,startupAttempts;
    private volatile boolean failed;
    private boolean finished;
    private volatile TileQuarry powered;
    @Override public void init(){if(Boolean.getBoolean("industrialtech.lampTest")){new LampClientChecks().init();return;}if(Boolean.getBoolean("industrialtech.reactorTest")){new ReactorClientTest().init();return;}if(Boolean.getBoolean("industrialtech.neiTest")){new NeiClientTest().init();return;}if(Boolean.getBoolean("industrialtech.codeTest")){new CodeClientTest().init();return;}
        if(Boolean.getBoolean("industrialtech.wirelessTest")){new WirelessClientTest().init();return;}FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void supply(TickEvent.ServerTickEvent event) {
        TileQuarry q=powered;
        if(event.phase==TickEvent.Phase.START&&q!=null&&!q.isInvalid())q.receiveEnergy(net.minecraftforge.common.util.ForgeDirection.NORTH,20000,false);
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END||finished||--wait>0)return;
        Minecraft mc=Minecraft.getMinecraft();
        try{
            if(failed){finish(mc);return;}
            if(phase==0){
                if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}
                mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;
                WorldSettings settings=new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);
                settings.func_82750_a("2;7;1;");mc.launchIntegratedServer("gui-integration","GUI Integration",settings);phase=1;wait=60;return;
            }
            if(mc.thePlayer==null||mc.theWorld==null){if(++startupAttempts>60)throw new AssertionError("Test client did not join its integrated world");wait=20;return;}
            if(phase==1){
                mc.displayGuiScreen(null);
                ServerWork.enqueue(()->{
                    WorldServer world=serverWorld();
                    for(int x=21;x<=39;x++)for(int z=15;z<=27;z++)world.setBlock(x,71,z,Blocks.stone,0,3);
                    for(int i=0;i<3;i++){
                        world.setBlock(24+i*4,72,24,ModRegistry.quarries[i],2,3);
                        TileQuarry q=(TileQuarry)world.getTileEntity(24+i*4,72,24);q.energy=12345;
                        for(int s=0;s<9;s++)q.setInventorySlotContents(s,new ItemStack(s==0?Items.diamond:Items.iron_ingot,s==0?20:s*4));q.markDirty();
                    }
                    world.setBlock(36,72,24,ModRegistry.loader,2,3);movePlayer(24);
                });phase=2;wait=60;return;
            }
            if(phase==2){quarrySteps(mc);return;}
            if(phase==3){loaderSteps(mc);return;}
            if(phase==4){
                shot(mc,"machines-and-preview.png");
                GuiInventory vanilla=vanillaInventory(mc);
                renderShot(mc,vanilla,"vanilla-inventory-after.png",false);comparePanel(mc,"vanilla-inventory-before.png","vanilla-inventory-after.png",72,37,false);
                pass("Closing machine screens preserves vanilla inventory colors");
                ServerWork.enqueue(()->{
                    for(int i=0;i<3;i++){TileQuarry q=(TileQuarry)machine(24+i*4);q.applySelection(ChunkArea.CENTER|1|(1<<24));q.toggleEnabled();}
                    machine(36).applySelection(ChunkArea.CENTER|1|(1<<24));machine(36).toggleEnabled();
                });phase=5;wait=25;return;
            }
            if(phase==5){
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()>0,"public active boundaries missing outside GUI");
                shot(mc,"machines-active-red-borders.png");assertCrystalCyan(mc,"machines-active-red-borders.png");
                pass("Crystal facets render cyan in the actual world framebuffer");pass("Public red boundaries render without any GUI open");phase=6;wait=15;return;
            }
            if(phase==6){
                shot(mc,"crystal-active-later.png");ServerWork.enqueue(()->serverPlayer().setPositionAndUpdate(286.5,74,274.5));phase=8;wait=60;return;
            }
            if(phase==8){
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==0,"distant boundaries are not culled");
                ServerWork.enqueue(()->serverPlayer().setPositionAndUpdate(30.5,74,18.5));phase=9;wait=60;return;
            }
            if(phase==9){
                TileMachine tile=(TileMachine)mc.theWorld.getTileEntity(36,72,24);
                assertTrue(tile!=null&&tile.enabled&&Integer.bitCount(tile.loadedMask)==3&&dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()>0,"returning chunk watch did not restore public overlay state");
                pass("Leaving and returning to watched chunks restores public overlays without opening a GUI");
                shot(mc,"machines-after-range-reload.png");
                ServerWork.enqueue(()->{for(int i=0;i<3;i++)machine(24+i*4).toggleEnabled();machine(36).toggleEnabled();});phase=7;wait=25;return;
            }
            if(phase==7){
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==0,"stopped machines retain active geometry");
                shot(mc,"machines-stopped.png");assertCrystalCyan(mc,"machines-stopped.png");renderShot(mc,vanillaInventory(mc),"vanilla-inventory-after-world-renderers.png",false);
                comparePanel(mc,"vanilla-inventory-before.png","vanilla-inventory-after-world-renderers.png",72,37,false);
                pass("Stopping every machine hides red frames and world renderers preserve vanilla GUI colors");finish(mc);
            }
        }catch(Throwable error){failed=true;results.add("FAIL: client phase "+phase+" step "+step+" :: "+error);shot(mc,"client-test-error.png");IndustrialTechnology.log.error("CLIENT TEST FAILED",error);finish(mc);}
    }
    private void quarrySteps(Minecraft mc)throws Exception{
        int x=24+tier*4;
        switch(step){
            case 0:
                if(tier==0){GuiInventory vanilla=vanillaInventory(mc);renderShot(mc,vanilla,"vanilla-inventory-before.png",false);}
                ServerWork.enqueue(()->{movePlayer(x);powered=(TileQuarry)machine(x);});break;
            case 1:mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(x,72,24,2,null,.5f,.5f,.5f));break;
            case 2:{
                GuiMachine gui=gui(mc);assertTrue(gui.container.inventorySlots.size()==90+new int[]{18,36,50}[tier],"incorrect output/player/ghost slot count");
                assertTrue(gui.width>=316&&gui.height>=236,"GUI exceeds minimum scaled display");
                pass("MK"+(tier+1)+" GUI opens and renders at GUI scale 3");shot(mc,"quarry-mk"+(tier+1)+"-control.png");click(gui,3);break;
            }
            case 3:{
                assertTrue(state(gui(mc)).getBoolean("Silk"),"Silk GUI did not sync");
                renderShot(mc,gui(mc),"quarry-mk"+(tier+1)+"-silk-tint.png",true);
                comparePanel(mc,"quarry-mk"+(tier+1)+"-control.png","quarry-mk"+(tier+1)+"-silk-tint.png",left(gui(mc)),top(gui(mc)),true);
                pass("MK"+(tier+1)+" GUI resets incoming tint and preserves panel/slot colors");
                serverCheck("MK"+(tier+1)+" Silk button server synchronization",()->assertTrue(((TileQuarry)machine(x)).usesSilk(),"Silk server mode false"));
                click(gui(mc),2);break;
            }
            case 4:{
                GuiMachine gui=gui(mc);assertTrue(!state(gui).getBoolean("Silk"),"Fortune GUI did not sync");
                serverCheck("MK"+(tier+1)+" Fortune button mutual exclusion",()->assertTrue(((TileQuarry)machine(x)).fortuneLevel()==tier+1&&!((TileQuarry)machine(x)).usesSilk(),"Fortune server mode wrong"));
                click(gui,21);assertTrue(Integer.bitCount((Integer)field(GuiMachine.class,"draft").get(gui))==9,"3x3 preset failed");
                click(gui,23);assertTrue((Integer)field(GuiMachine.class,"draft").get(gui)==ChunkArea.CENTER,"Clear preset failed");
                click(gui,20);assertTrue((Integer)field(GuiMachine.class,"draft").get(gui)==ChunkArea.CENTER,"Select 1 failed");
                click(gui,22);for(int cell=15;cell<25;cell++)cellClick(gui,cell);cellClick(gui,12);
                assertTrue(Integer.bitCount((Integer)field(GuiMachine.class,"draft").get(gui))==15,"chunk-grid selection wrong");
                assertTrue(state(gui).getInteger("Selected")==ChunkArea.CENTER,"draft applied without Apply");click(gui,1);break;
            }
            case 5:{
                assertTrue(Integer.bitCount(state(gui(mc)).getInteger("Selected"))==15,"Apply GUI did not sync");
                serverCheck("MK"+(tier+1)+" individual 15-chunk grid and Apply",()->assertTrue(Integer.bitCount(machine(x).selected)==15,"Apply server wrong"));
                click(gui(mc),0);break;
            }
            case 6:{
                serverCheck("MK"+(tier+1)+" Start button and exact chunk loading",()->assertTrue(machine(x).enabled&&machine(x).loadedCount==15,"Start or loading failed"));
                TileMachine publicTile=(TileMachine)mc.theWorld.getTileEntity(x,72,24);
                assertTrue(publicTile.enabled&&Integer.bitCount(publicTile.selected)==15&&dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()>0,"nearby non-owner did not receive quarry overlay state");
                pass("MK"+(tier+1)+" public quarry boundaries follow real description packets");
                shot(mc,"quarry-mk"+(tier+1)+"-running.png");
                comparePanel(mc,"quarry-mk"+(tier+1)+"-control.png","quarry-mk"+(tier+1)+"-running.png",left(gui(mc)),top(gui(mc)),true);
                click(gui(mc),0);break;
            }
            case 7:{
                GuiMachine gui=gui(mc);IndustrialTechnology.network.sendToServer(new ControlPacket(gui.container.windowId,ControlPacket.APPLY,0));
                IndustrialTechnology.network.sendToServer(new ControlPacket(gui.container.windowId+1,ControlPacket.TOGGLE,0));
                mc.playerController.windowClick(gui.container.windowId,0,0,0,mc.thePlayer);break;
            }
            case 8:{
                assertTrue(!((TileMachine)mc.theWorld.getTileEntity(x,72,24)).enabled&&dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==0,"quarry STOP frame remains visible");
                serverCheck("MK"+(tier+1)+" Stop, invalid packets, hidden-slot protection",()->assertTrue(!machine(x).enabled&&Integer.bitCount(machine(x).selected)==15&&((TileQuarry)machine(x)).getStackInSlot(0).stackSize==20,"validation or hidden-slot extraction failed"));
                click(gui(mc),11);break;
            }
            case 9:{
                GuiMachine gui=gui(mc);assertTrue(gui.container.tab==1,"Output tab not synchronized");
                shot(mc,"quarry-mk"+(tier+1)+"-output.png");diamondsBefore=playerDiamonds(mc);
                comparePanel(mc,"quarry-mk"+(tier+1)+"-control.png","quarry-mk"+(tier+1)+"-output.png",left(gui(mc)),top(gui(mc)),true);
                mc.playerController.windowClick(gui.container.windowId,0,0,1,mc.thePlayer);break;
            }
            case 10:{
                assertTrue(playerDiamonds(mc)==diamondsBefore+20,"shift-click client inventory lost or duplicated items");
                serverCheck("MK"+(tier+1)+" output tab and shift-click extraction",()->assertTrue(((TileQuarry)machine(x)).getStackInSlot(0)==null,"shift-click server did not extract"));
                click(gui(mc),12);break;
            }
            case 11:{
                GuiMachine g=gui(mc);assertTrue(g.container.tab==2&&!state(g).getBoolean("VoidEnabled"),"filter tab/default state wrong");
                renderShot(mc,g,"quarry-mk"+(tier+1)+"-void-filter.png",true);
                comparePanel(mc,"quarry-mk"+(tier+1)+"-control.png","quarry-mk"+(tier+1)+"-void-filter.png",left(g),top(g),true);
                pass("MK"+(tier+1)+" Void Filter tab preserves the industrial palette");
                filterSourceSlot=-1;filterDiamondTotal=playerDiamonds(mc);
                for(int i=54;i<90;i++){ItemStack s=((net.minecraft.inventory.Slot)g.container.inventorySlots.get(i)).getStack();if(s!=null&&s.getItem()==Items.diamond){filterSourceSlot=i;carriedBefore=s.stackSize;break;}}
                assertTrue(filterSourceSlot>=0,"filter source item missing");mc.playerController.windowClick(g.container.windowId,filterSourceSlot,0,0,mc.thePlayer);break;
            }
            case 12:
                assertTrue(mc.thePlayer.inventory.getItemStack()!=null&&mc.thePlayer.inventory.getItemStack().stackSize==carriedBefore,"source item did not move to cursor");
                mc.playerController.windowClick(gui(mc).container.windowId,90,0,0,mc.thePlayer);break;
            case 13:{
                ItemStack icon=((TileQuarry)gui(mc).container.machine).filter.getStackInSlot(0);
                assertTrue(icon!=null&&icon.getItem()==Items.diamond&&icon.stackSize==1&&mc.thePlayer.inventory.getItemStack().stackSize==carriedBefore,"ghost packet consumed/duplicated item");
                serverCheck("MK"+(tier+1)+" real ghost-slot packets synchronize without consuming the carried item",()->assertTrue(((TileQuarry)machine(x)).filter.getStackInSlot(0).stackSize==1&&serverPlayer().inventory.getItemStack().stackSize==carriedBefore,"server ghost state corrupt"));
                break;
            }
            case 14:
                mc.playerController.windowClick(gui(mc).container.windowId,90,1,0,mc.thePlayer);break;
            case 15:
                assertTrue(((TileQuarry)gui(mc).container.machine).filter.getStackInSlot(0)==null,"right-click did not remove ghost");
                mc.playerController.windowClick(gui(mc).container.windowId,90,0,0,mc.thePlayer);click(gui(mc),5);click(gui(mc),6);break;
            case 16:
                assertTrue(state(gui(mc)).getBoolean("VoidEnabled")&&state(gui(mc)).getBoolean("VoidIgnoreMetadata"),"filter controls not synchronized");
                renderShot(mc,gui(mc),"quarry-mk"+(tier+1)+"-void-filter-active.png",false);
                pass("MK"+(tier+1)+" Auto Delete and metadata controls synchronize");
                mc.playerController.windowClick(gui(mc).container.windowId,filterSourceSlot,0,0,mc.thePlayer);click(gui(mc),7);break;
            case 17:
                assertTrue(mc.thePlayer.inventory.getItemStack()==null&&playerDiamonds(mc)==filterDiamondTotal&&state(gui(mc)).getInteger("VoidConfigured")==0,"Clear All changed real items or retained icons");
                pass("MK"+(tier+1)+" right-click and Clear All preserve real inventory");click(gui(mc),5);break;
            case 18:
                assertTrue(!state(gui(mc)).getBoolean("VoidEnabled"),"filter remained enabled");
                if(tier==0){step=23;break;}click(gui(mc),9);break;
            case 19:
                assertTrue(gui(mc).container.filterPage==1,"Next did not select page two");
                mc.playerController.windowClick(gui(mc).container.windowId,filterSourceSlot,0,0,mc.thePlayer);break;
            case 20:
                mc.playerController.windowClick(gui(mc).container.windowId,90+new int[]{18,36,50}[tier]-1,0,0,mc.thePlayer);break;
            case 21:{
                int last=new int[]{18,36,50}[tier]-1;ItemStack icon=((TileQuarry)gui(mc).container.machine).filter.getStackInSlot(last);
                assertTrue(icon!=null&&icon.stackSize==1&&mc.thePlayer.inventory.getItemStack().stackSize==carriedBefore,"page-two ghost changed real items");
                serverCheck("MK"+(tier+1)+" final tier filter slot synchronizes on page two without consuming items",()->assertTrue(((TileQuarry)machine(x)).filter.getStackInSlot(last)!=null,"final server entry missing"));
                shot(mc,"quarry-mk"+(tier+1)+"-void-filter-page2.png");mc.playerController.windowClick(gui(mc).container.windowId,filterSourceSlot,0,0,mc.thePlayer);break;
            }
            case 22:click(gui(mc),8);click(gui(mc),7);break;
            case 23:
                assertTrue(gui(mc).container.filterPage==0&&state(gui(mc)).getInteger("VoidConfigured")==0&&mc.thePlayer.inventory.getItemStack()==null&&playerDiamonds(mc)==filterDiamondTotal,"pagination clear/return corrupted filter or items");
                pass("MK"+(tier+1)+" Previous/Next filter pages and cross-page Clear All work in the real GUI");break;
            case 24:{mc.thePlayer.closeScreen();tier++;step=-1;if(tier==3){phase=3;step=-1;}break;}
        }
        step++;wait=25;
    }
    private void loaderSteps(Minecraft mc)throws Exception{
        switch(step){
            case 0:ServerWork.enqueue(()->movePlayer(36));break;
            case 1:mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(36,72,24,2,null,.5f,.5f,.5f));break;
            case 2:{
                GuiMachine g=gui(mc);assertTrue(g instanceof GuiChunkLoader&&g.container.inventorySlots.isEmpty(),"loader GUI still exposes inventory");
                assertTrue(!state(g).hasKey("Energy")&&!state(g).hasKey("Capacity")&&!state(g).hasKey("Input"),"loader screen has RF state");
                renderShot(mc,g,"chunk-loader-disabled.png",false);renderShot(mc,g,"chunk-loader-tint.png",true);
                compareLoader(mc,"chunk-loader-disabled.png","chunk-loader-tint.png",left(g),top(g));
                pass("Compact zero-energy loader screen and stable OpenGL colors");
                click(g,22);for(int c=15;c<25;c++)cellClick(g,c);click(g,1);break;
            }
            case 3:click(gui(mc),0);break;
            case 4:{
                serverCheck("Chunk Loader GUI Apply/Enable and exact 15 chunks",()->assertTrue(machine(36).enabled&&machine(36).loadedCount==15,"loader controls failed"));
                shot(mc,"chunk-loader-tooltip.png");renderShot(mc,gui(mc),"chunk-loader-control.png",false);
                compareLoader(mc,"chunk-loader-disabled.png","chunk-loader-control.png",left(gui(mc)),top(gui(mc)));
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==38&&((TileMachine)mc.theWorld.getTileEntity(36,72,24)).loadedCount==15,"15-chunk public loader geometry wrong");
                pass("Public loader borders match fifteen actual loaded chunks");click(gui(mc),25);break;
            }
            case 5:
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==0,"local Hide left loader borders visible");
                serverCheck("Local border toggle preserves active loader tickets",()->assertTrue(machine(36).enabled&&machine(36).loadedCount==15,"local toggle changed server state"));
                click(gui(mc),25);ServerWork.enqueue(()->machine(36).applySelection(ChunkArea.ALL));break;
            case 6:
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==60,"25-chunk loader geometry wrong");
                ServerWork.enqueue(()->machine(36).applySelection(ChunkArea.CENTER|1|(1<<24)));break;
            case 7:
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==12,"irregular loader geometry includes excluded chunks");
                ServerWork.enqueue(()->machine(36).applySelection(ChunkArea.CENTER));break;
            case 8:
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==4,"one-chunk loader geometry wrong");
                pass("Public loader borders update for one, twenty-five, and irregular chunk selections");
                assertTrue(dev.industrialtechnology.client.ChunkLoaderRenderer.angle(false,10)==0&&dev.industrialtechnology.client.ChunkLoaderRenderer.angle(false,75)==0
                    &&dev.industrialtechnology.client.ChunkLoaderRenderer.angle(true,25)==90&&dev.industrialtechnology.client.ChunkLoaderRenderer.bob(false,25)==0,"crystal animation state wrong");
                pass("Crystal uses a stationary OFF pose and five-second interpolated ON rotation");click(gui(mc),0);break;
            case 9:
                assertTrue(dev.industrialtechnology.client.ClientProxy.boundaries.segmentCount()==0,"OFF retains loader geometry");
                serverCheck("Chunk Loader OFF immediately releases its own tickets",()->assertTrue(!machine(36).enabled&&machine(36).loadedCount==0,"loader OFF failed"));
                click(gui(mc),24);assertTrue(dev.industrialtechnology.client.AreaPreview.isActive(gui(mc).container.machine),"preview did not enable");
                click(gui(mc),24);assertTrue(!dev.industrialtechnology.client.AreaPreview.isActive(gui(mc).container.machine),"preview did not disable");
                click(gui(mc),24);pass("Area preview ON/OFF toggle");mc.thePlayer.closeScreen();
                ServerWork.enqueue(()->{
                    for(int x=21;x<=39;x++)for(int z=15;z<=27;z++)serverWorld().setBlock(x,71,z,Blocks.stone,0,3);
                    EntityPlayerMP p=serverPlayer();p.capabilities.isFlying=true;p.sendPlayerAbilities();p.setPositionAndUpdate(30.5,74,18.5);
                });break;
            case 10:mc.thePlayer.capabilities.isFlying=true;mc.thePlayer.rotationYaw=0;mc.thePlayer.rotationPitch=17;phase=4;break;
        }
        step++;wait=25;
    }
    private static GuiMachine gui(Minecraft mc){assertTrue(mc.currentScreen instanceof GuiMachine,"machine GUI missing; screen="+mc.currentScreen);return(GuiMachine)mc.currentScreen;}
    private static NBTTagCompound state(GuiMachine gui)throws Exception{return(NBTTagCompound)field(GuiMachine.class,"state").get(gui);}
    private static Field field(Class<?> owner,String name)throws Exception{Field f=owner.getDeclaredField(name);f.setAccessible(true);return f;}
    private static void click(GuiMachine gui,int id)throws Exception{
        List<?> buttons=(List<?>)field(GuiScreen.class,"buttonList").get(gui);
        for(Object b:buttons)if(((GuiButton)b).id==id){assertTrue(((GuiButton)b).enabled,"button disabled: "+id);Method m=GuiMachine.class.getDeclaredMethod("actionPerformed",GuiButton.class);m.setAccessible(true);m.invoke(gui,b);return;}
        throw new AssertionError("button missing: "+id);
    }
    private static void cellClick(GuiMachine gui,int cell)throws Exception{
        int left=field(GuiContainer.class,"guiLeft").getInt(gui),top=field(GuiContainer.class,"guiTop").getInt(gui);
        Method m=GuiMachine.class.getDeclaredMethod("mouseClicked",int.class,int.class,int.class);m.setAccessible(true);
        int gx=field(GuiMachine.class,"gridX").getInt(gui),gy=field(GuiMachine.class,"gridY").getInt(gui),size=field(GuiMachine.class,"cellSize").getInt(gui);
        m.invoke(gui,left+gx+1+cell%5*size,top+gy+1+cell/5*size,0);
    }
    private void serverCheck(String name,Runnable assertion){ServerWork.enqueue(()->{try{assertion.run();pass(name);}catch(Throwable e){failed=true;results.add("FAIL: "+name+" :: "+e);IndustrialTechnology.log.error("CLIENT SERVER CHECK FAILED",e);}});}
    private static WorldServer serverWorld(){return MinecraftServer.getServer().worldServerForDimension(0);}
    private static TileMachine machine(int x){return(TileMachine)serverWorld().getTileEntity(x,72,24);}
    private static EntityPlayerMP serverPlayer(){return(EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);}
    private static void movePlayer(int x){EntityPlayerMP p=serverPlayer();p.capabilities.isFlying=true;p.sendPlayerAbilities();p.setPositionAndUpdate(x+.5,73,22.5);}
    private static int playerDiamonds(Minecraft mc){int count=0;for(ItemStack s:mc.thePlayer.inventory.mainInventory)if(s!=null&&s.getItem()==Items.diamond)count+=s.stackSize;return count;}
    private void pass(String name){results.add("PASS: "+name);IndustrialTechnology.log.info("CLIENT TEST PASS: "+name);}
    private static void assertTrue(boolean value,String why){if(!value)throw new AssertionError(why);}
    private void shot(Minecraft mc,String name){ScreenShotHelper.saveScreenshot(mc.mcDataDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());}
    private static int left(GuiMachine g)throws Exception{return field(GuiContainer.class,"guiLeft").getInt(g);}
    private static int top(GuiMachine g)throws Exception{return field(GuiContainer.class,"guiTop").getInt(g);}
    private static GuiInventory vanillaInventory(Minecraft mc) {
        // Render the vanilla survival panel without its creative-mode redirect or changing the live open container.
        GuiInventory screen=new GuiInventory(mc.thePlayer) {
            @Override public void initGui(){buttonList.clear();guiLeft=(width-xSize)/2;guiTop=(height-ySize)/2;}
        };
        screen.setWorldAndResolution(mc,320,240);return screen;
    }
    private void renderShot(Minecraft mc,GuiScreen screen,String file,boolean tint) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        mc.getFramebuffer().bindFramebuffer(true);
        GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glOrtho(0,320,240,0,1000,3000);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glTranslatef(0,0,-2000);
        try {
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);GL11.glColor4f(tint?.2f:1,tint?.75f:1,tint?.3f:1,1);
            screen.drawScreen(0,0,0);
            if(tint){FloatBuffer color=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);assertTrue(Math.abs(color.get(0)-.2f)<.01&&Math.abs(color.get(1)-.75f)<.01,"GUI failed to restore incoming OpenGL color");}
            shot(mc,file);
        }finally{
            GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);mc.getFramebuffer().unbindFramebuffer();GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);
        }
    }
    private void comparePanel(Minecraft mc,String a,String b,int x,int y,boolean machine)throws Exception{
        int[][] points=machine?new int[][]{{5,80},{9,139},{80,153},{97,170},{133,187},{151,210},{239,225}}:new int[][]{{4,4},{7,150},{22,90},{42,108},{61,127}};
        comparePixels(mc,a,b,x,y,points);
    }
    private void compareLoader(Minecraft mc,String a,String b,int x,int y)throws Exception{comparePixels(mc,a,b,x,y,new int[][]{{5,55},{11,61},{195,142},{17,193},{203,100}});}
    private void comparePixels(Minecraft mc,String a,String b,int x,int y,int[][] points)throws Exception{
        BufferedImage first=ImageIO.read(new File(mc.mcDataDir,"screenshots/"+a)),second=ImageIO.read(new File(mc.mcDataDir,"screenshots/"+b));
        for(int[] p:points){int px=(x+p[0])*3+1,py=(y+p[1])*3+1;assertTrue(first.getRGB(px,py)==second.getRGB(px,py),"GUI palette changed between "+a+" and "+b+" at "+p[0]+","+p[1]);}
    }
    private void assertCrystalCyan(Minecraft mc,String filename)throws Exception {
        BufferedImage shot=ImageIO.read(new File(mc.mcDataDir,"screenshots/"+filename));int cyan=0;
        // Fixed camera at (30.5,74,18.5), yaw 0, pitch 17. This region is above the loader, excluding its cyan corners and sky.
        for(int y=345;y<398;y++)for(int x=25;x<105;x++) {
            int pixel=shot.getRGB(x,y),r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
            if(g>80&&b>80&&g>r*1.4&&b>r*1.3)cyan++;
        }
        assertTrue(cyan>25,"crystal is not cyan in "+filename+" ("+cyan+" cyan pixels)");
    }
    private void finish(Minecraft mc){
        finished=true;try{results.add("Failed: "+(failed?1:0));Files.write(new File(mc.mcDataDir,"client-integration.txt").toPath(),results,StandardCharsets.UTF_8);}catch(Exception e){IndustrialTechnology.log.error("Client report failed",e);}
        mc.shutdown();
    }
}
