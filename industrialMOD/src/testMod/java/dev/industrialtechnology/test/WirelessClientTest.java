package dev.industrialtechnology.test;
import java.util.*;
import java.lang.reflect.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.network.play.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.client.GuiWirelessTerminal;
import dev.industrialtechnology.wireless.*;
public final class WirelessClientTest {
    private final List<String> results=Collections.synchronizedList(new ArrayList<String>());
    private final ClientTestProxy renderHelper=new ClientTestProxy();
    private int phase,step,wait=30,startup;private boolean finished;private volatile boolean failed;
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||finished||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();
        try{
            if(failed){finish(mc);return;}
            if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;
                WorldSettings settings=new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);settings.func_82750_a("2;7;1;");mc.launchIntegratedServer("wireless-client","Wireless Gameplay",settings);phase=1;wait=60;return;}
            if(mc.thePlayer==null||mc.theWorld==null){if(++startup>60)throw new AssertionError("Client failed to join");wait=20;return;}
            if(phase==1){mc.displayGuiScreen(null);ServerWork.enqueue(()->setup());phase=2;wait=60;return;}
            run(mc);step++;wait=step==24?95:25;
        }catch(Throwable error){failed=true;results.add("FAIL: wireless client step "+step+" :: "+error);IndustrialTechnology.log.error("WIRELESS CLIENT FAILED",error);shot(mc,"wireless-client-error.png");finish(mc);}
    }
    private static WorldServer world(){return MinecraftServer.getServer().worldServerForDimension(0);}
    private static EntityPlayerMP player(){return(EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);}
    private void setup(){
        WorldServer w=world();EntityPlayerMP p=player();for(int x=20;x<=35;x++)for(int z=20;z<=27;z++)w.setBlock(x,71,z,Blocks.stone,0,3);
        w.setBlock(24,72,24,Blocks.chest,0,3);TileEntityChest chest=(TileEntityChest)w.getTileEntity(24,72,24);chest.setInventorySlotContents(0,new ItemStack(Items.iron_ingot,64));chest.setInventorySlotContents(1,new ItemStack(Items.iron_ingot,64));chest.setInventorySlotContents(2,new ItemStack(Items.coal,32));
        w.setBlock(28,72,24,WirelessTestFixtures.tanks,0,3);w.setBlock(32,72,24,Blocks.stone,0,3);w.setBlock(32,72,22,Blocks.redstone_lamp,0,3);
        p.inventory.currentItem=0;p.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));p.inventory.setInventorySlotContents(1,new ItemStack(ModRegistry.greenStick,8));p.inventory.setInventorySlotContents(2,new ItemStack(ModRegistry.yellowStick,8));p.inventory.setInventorySlotContents(3,new ItemStack(ModRegistry.blueStick,8));p.inventory.setInventorySlotContents(4,new ItemStack(Blocks.stone,32));
        UUID net=WirelessAccess.terminal(p,p.getHeldItem(),true);p.getEntityData().setString("IndustrialTerminal",net.toString());p.capabilities.isFlying=true;p.sendPlayerAbilities();p.setPositionAndUpdate(28.5,73,22.5);p.setSneaking(false);p.inventory.markDirty();
        // A real saved chest is registered, then explicitly unloaded; opening its link must not reload it.
        w.getChunkFromChunkCoords(25,25);w.setBlock(400,72,400,Blocks.chest,0,3);WirelessEndpoint offline=new WirelessEndpoint();offline.owner=p.getUniqueID();offline.network=net;offline.dimension=0;offline.x=400;offline.y=72;offline.z=400;offline.side=2;offline.name="Warehouse (unloaded)";offline.machine="Chest";offline.block="minecraft:chest";offline.tileToken=TargetAnchors.bind(w.getTileEntity(400,72,400));WirelessData.get(w).add(offline);
        w.theChunkProviderServer.unloadChunksIfNotNearSpawn(25,25);w.theChunkProviderServer.unloadQueuedChunks();
    }
    private void run(Minecraft mc)throws Exception{
        switch(step){
            case 0:{GuiScreen vanilla=(GuiScreen)method(ClientTestProxy.class,"vanillaInventory",Minecraft.class).invoke(null,mc);render(mc,vanilla,"wireless-vanilla-before.png",false);MouseEvent input=new MouseEvent();Field button=field(MouseEvent.class,"button"),pressed=field(MouseEvent.class,"buttonstate");button.setInt(input,0);pressed.setBoolean(input,true);MinecraftForge.EVENT_BUS.post(input);check(input.isCanceled()&&!mc.gameSettings.keyBindAttack.getIsKeyPressed(),"attack input was not intercepted");break;}
            case 1:check(mc.currentScreen instanceof GuiWirelessTerminal,"left mouse event did not open terminal");pass("Left mouse input is canceled before attack/break processing and opens the actual terminal via a server message");mc.thePlayer.closeScreen();hold(mc,1);place(mc,24,72,24,2);break;
            case 2:place(mc,28,72,24,2);break;
            case 3:hold(mc,2);place(mc,32,72,24,2);break;
            case 4:hold(mc,3);place(mc,24,72,24,5);break;
            case 5:hold(mc,0);right(mc);break;
            case 6:{GuiWirelessTerminal g=gui(mc);check(g.state.getInteger("Total")==5,"actual stick placement packets failed");pass("Normal non-sneaking PlayerController right-clicks send real packets, place all three colored torches, and link machine faces");server("Green and Blue are real placed blocks without opening the chest",()->check(world().getBlock(24,72,23)==ModRegistry.monitoringStick&&world().getBlock(25,72,24)==ModRegistry.automationStick,"missing physical Green/Blue torches"));
                // The offline monitor was registered first; choose the chest monitor through the actual list click.
                mouse(g,20,48+14+4);break;}
            case 7:{GuiWirelessTerminal g=gui(mc);check("ONLINE".equals(g.state.getString("Status"))&&g.state.getInteger("Slots")==27&&g.state.getTagList("Items",10).getCompoundTagAt(0).getInteger("Count")==64,"chest monitoring fabricated contents");check(g.terminal.inventorySlots.size()==37,"target inventory exposed as transferable slots");
                render(mc,g,"wireless-monitors-items.png",false);render(mc,g,"wireless-monitors-tint.png",true);pass("Real chest item icons and counts are read-only, with incoming OpenGL tint restored");((GuiTextField)field(GuiWirelessTerminal.class,"name").get(g)).setText("Ore Input Chest");click(g,6);break;}
            case 8:check(gui(mc).state.getString("Name").equals("Ore Input Chest"),"rename did not synchronize");pass("Device renaming synchronizes from the authoritative server registry");mouse(gui(mc),20,48+28+4);break;
            case 9:click(gui(mc),19);break;
            case 10:{GuiWirelessTerminal g=gui(mc);check(g.state.getTagList("Tanks",10).tagCount()==2&&g.state.getTagList("Tanks",10).getCompoundTagAt(0).getLong("Amount")==1000,"multi-tank data wrong");render(mc,g,"wireless-monitors-tanks.png",false);compare(mc,"wireless-monitors-items.png","wireless-monitors-tanks.png",g);pass("Multiple actual Forge tanks display their own amounts and capacities without tinting the player inventory");click(g,2);break;}
            case 11:mouse(gui(mc),275,122);break;
            case 12:{GuiWirelessTerminal g=gui(mc);check(g.state.getBoolean("Signal"),"ON control not synchronized");server("Terminal ON sends real redstone power 15 and lights a real lamp",()->check(world().getBlock(32,72,22)==Blocks.lit_redstone_lamp,"lamp stayed OFF"));render(mc,g,"wireless-control-on.png",false);click(g,9);break;}
            case 13:check(gui(mc).state.getBoolean("PulseMode")&&!gui(mc).state.getBoolean("Signal"),"mode change did not stop output");click(gui(mc),10);break;
            case 14:click(gui(mc),10);break;
            case 15:check(gui(mc).state.getInteger("Seconds")==7,"pulse duration edit failed");render(mc,gui(mc),"wireless-control-pulse-settings.png",false);pass("Yellow settings select pulse mode and a separate seven-second duration");click(gui(mc),3);break;
            case 16:mc.playerController.windowClick(gui(mc).terminal.windowId,31,0,0,mc.thePlayer);break;
            case 17:mc.playerController.windowClick(gui(mc).terminal.windowId,36,0,0,mc.thePlayer);click(gui(mc),17);break;
            case 18:{GuiWirelessTerminal g=gui(mc);check(g.terminal.ghost.getStackInSlot(0)!=null&&g.terminal.ghost.getStackInSlot(0).stackSize==1&&mc.thePlayer.inventory.getItemStack().stackSize==32,"automation ghost consumed items");mouse(g,135,65);pass("Automation ghost selection copies an icon without consuming or duplicating the real carried stack");break;}
            case 19:check(!gui(mc).state.getString("Target").isEmpty(),"linked Yellow target picker failed");click(gui(mc),15);click(gui(mc),16);break;
            case 20:{GuiWirelessTerminal g=gui(mc);check(g.state.getBoolean("RuleEnabled")&&g.state.getInteger("Minimum")==1,"rule did not save");render(mc,g,"wireless-automation-rule.png",false);mc.playerController.windowClick(g.terminal.windowId,31,0,0,mc.thePlayer);server("GUI saves a real enabled contains-stone rule and preserves all player items",()->{int total=0;for(ItemStack s:player().inventory.mainInventory)if(s!=null&&s.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.stone))total+=s.stackSize;ItemStack carried=player().inventory.getItemStack();if(carried!=null&&carried.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.stone))total+=carried.stackSize;check(total==32,"real stone count changed");((TileEntityChest)world().getTileEntity(24,72,24)).setInventorySlotContents(3,new ItemStack(Blocks.stone));});click(g,2);break;}
            case 21:check(mc.thePlayer.inventory.getItemStack()==null&&mc.thePlayer.inventory.getStackInSlot(4).stackSize==32,"returned cursor stack was lost");check(gui(mc).state.getBoolean("Signal")&&gui(mc).state.getLong("Remaining")>0,"real Blue rule did not start pulse");render(mc,gui(mc),"wireless-control-active-pulse.png",false);pass("Real chest insertion activates the GUI-linked Yellow relay using its configured pulse timer");mc.thePlayer.closeScreen();ServerWork.enqueue(()->player().setPositionAndUpdate(30.5,73,20.5));break;
            case 22:mc.thePlayer.capabilities.isFlying=true;mc.thePlayer.rotationYaw=-31.6f;mc.thePlayer.rotationPitch=25.2f;break;
            case 23:shot(mc,"wireless-yellow-torch-world.png");assertTorchVisible(mc);pass("The placed Yellow relay renders with the classic colored redstone-torch mesh");break;
            case 24:server("Pulse turns OFF automatically and a permanently true condition does not retrigger",()->check(!WirelessAccess.relay(control()).signal()&&world().getBlock(32,72,22)==Blocks.redstone_lamp,"pulse stayed ON or retriggered"));ServerWork.enqueue(()->player().setPositionAndUpdate(64.5,73,24.5));break;
            case 25:right(mc);break;
            case 26:check(gui(mc).state.getString("Name").equals("Warehouse (unloaded)"),"initial device selection changed");check("OFFLINE".equals(gui(mc).state.getString("Status")),"unloaded target is not offline");render(mc,gui(mc),"wireless-monitor-offline.png",false);server("Actual unloaded device stays OFFLINE and opening its page does not load the chunk",()->check(!world().getChunkProvider().chunkExists(25,25),"terminal force-loaded target"));mouse(gui(mc),20,66);break;
            case 27:check(gui(mc).state.getString("Name").equals("Ore Input Chest")&&"ONLINE".equals(gui(mc).state.getString("Status")),"distant loaded monitor unavailable");pass("Renamed links reopen and monitor a loaded machine from outside normal interaction range");click(gui(mc),3);break;
            case 28:click(gui(mc),13);break;
            case 29:check(gui(mc).state.getInteger("Rule")==1&&!gui(mc).state.getBoolean("RuleEnabled"),"next rule not independent");click(gui(mc),12);break;
            case 30:check(gui(mc).state.getInteger("Rule")==0&&gui(mc).state.getBoolean("RuleEnabled")&&gui(mc).terminal.ghost.getStackInSlot(0)!=null,"saved rule icon lost on navigation");pass("Rule navigation keeps independent settings and restores the saved ghost item");IndustrialTechnology.network.sendToServer(new WirelessCommand(gui(mc).terminal.windowId+1,WirelessCommand.TOGGLE,0,0,""));mc.thePlayer.closeScreen();break;
            case 31:{GuiScreen vanilla=(GuiScreen)method(ClientTestProxy.class,"vanillaInventory",Minecraft.class).invoke(null,mc);render(mc,vanilla,"wireless-vanilla-after.png",false);method(ClientTestProxy.class,"comparePanel",Minecraft.class,String.class,String.class,int.class,int.class,boolean.class).invoke(renderHelper,mc,"wireless-vanilla-before.png","wireless-vanilla-after.png",72,37,false);pass("Closing terminal tabs and rendering colored torches preserves vanilla inventory colors");ServerWork.enqueue(()->{player().setPositionAndUpdate(24.5,73,18.5);player().getEntityData().setTag("TestDetachedTerminal",player().inventory.getStackInSlot(0).writeToNBT(new NBTTagCompound()));player().inventory.setInventorySlotContents(0,null);player().inventory.markDirty();});break;}
            case 32:hold(mc,1);place(mc,22,71,21,1);break;
            case 33:hold(mc,2);place(mc,24,71,21,1);break;
            case 34:hold(mc,3);place(mc,26,71,21,1);break;
            case 35:server("Actual client right-click places all three colors on the ground without any terminal",()->{
                check(player().inventory.getStackInSlot(0)==null,"terminal fixture still present");
                for(int i=0;i<3;i++){int x=22+i*2;net.minecraft.block.Block expected=new net.minecraft.block.Block[]{ModRegistry.monitoringStick,ModRegistry.redstoneRelay,ModRegistry.automationStick}[i];check(world().getBlock(x,72,21)==expected&&((TileRedstoneRelay)world().getTileEntity(x,72,21)).endpoint==null,"unlinked floor placement failed");}
            });ServerWork.enqueue(()->player().setPositionAndUpdate(24.5,73,18.5));break;
            case 36:mc.thePlayer.rotationYaw=0;mc.thePlayer.rotationPitch=34;break;
            case 37:shot(mc,"wireless-three-torches-ground.png");ServerWork.enqueue(()->{player().inventory.setInventorySlotContents(0,ItemStack.loadItemStackFromNBT(player().getEntityData().getCompoundTag("TestDetachedTerminal")));player().inventory.markDirty();});break;
            case 38:hold(mc,0);place(mc,22,72,21,1);break;
            case 39:place(mc,24,72,21,1);break;
            case 40:place(mc,26,72,21,1);break;
            case 41:server("Normal client terminal right-click links existing ground torches rather than swallowing the click",()->{
                for(int i=0;i<3;i++){TileRedstoneRelay tile=(TileRedstoneRelay)world().getTileEntity(22+i*2,72,21);WirelessEndpoint e=WirelessData.get(world()).devices.get(tile.endpoint);check(e!=null&&e.physical&&e.owner.equals(player().getUniqueID()),"terminal block-use linking failed");}
            });break;
            case 42:finish(mc);break;
        }
    }
    private static void assertTorchVisible(Minecraft mc)throws Exception{
        java.awt.image.BufferedImage im=javax.imageio.ImageIO.read(new File(mc.mcDataDir,"screenshots/wireless-yellow-torch-world.png"));int yellow=0,brown=0;
        for(int y=300;y<420;y++)for(int x=420;x<540;x++){int pixel=im.getRGB(x,y),r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
            if(r>140&&g>120&&b<130&&r>g*.9)yellow++;
            if(r>40&&r<170&&g>25&&g<135&&b<95&&r>g*1.15&&g>b*1.2)brown++;
        }check(yellow>15&&brown>15,"torch cap or wooden stem missing from actual framebuffer: "+yellow+"/"+brown);
    }
    private static WirelessEndpoint control(){UUID net=WirelessAccess.terminal(player(),player().getHeldItem(),false);for(WirelessEndpoint e:WirelessData.get(world()).linked(net))if(e.kind==WirelessEndpoint.Kind.CONTROL)return e;throw new AssertionError("Control missing");}
    private static void hold(Minecraft mc,int slot){mc.thePlayer.inventory.currentItem=slot;mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));}
    private static void place(Minecraft mc,int x,int y,int z,int side){
        mc.thePlayer.setSneaking(false);
        mc.playerController.onPlayerRightClick(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem(),x,y,z,side,net.minecraft.util.Vec3.createVectorHelper(x+.5,y+.5,z+.5));
    }
    private static void right(Minecraft mc){mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(-1,-1,-1,255,mc.thePlayer.getHeldItem(),0,0,0));}
    private static GuiWirelessTerminal gui(Minecraft mc){check(mc.currentScreen instanceof GuiWirelessTerminal,"Terminal GUI missing: "+mc.currentScreen);return(GuiWirelessTerminal)mc.currentScreen;}
    private static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
    private static Method method(Class<?> c,String name,Class<?>...args)throws Exception{Method m=c.getDeclaredMethod(name,args);m.setAccessible(true);return m;}
    private static void click(GuiWirelessTerminal g,int id)throws Exception{List<?> buttons=(List<?>)field(GuiScreen.class,"buttonList").get(g);for(Object value:buttons){GuiButton b=(GuiButton)value;if(b.id==id){check(b.visible&&b.enabled,"Button unavailable: "+id);method(GuiWirelessTerminal.class,"actionPerformed",GuiButton.class).invoke(g,b);return;}}throw new AssertionError("Button missing");}
    private static void mouse(GuiWirelessTerminal g,int x,int y)throws Exception{int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);method(GuiWirelessTerminal.class,"mouseClicked",int.class,int.class,int.class).invoke(g,left+x,top+y,0);}
    private void render(Minecraft mc,GuiScreen screen,String file,boolean tint)throws Exception{method(ClientTestProxy.class,"renderShot",Minecraft.class,GuiScreen.class,String.class,boolean.class).invoke(renderHelper,mc,screen,file,tint);}
    private void compare(Minecraft mc,String a,String b,GuiWirelessTerminal g)throws Exception{int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);method(ClientTestProxy.class,"comparePanel",Minecraft.class,String.class,String.class,int.class,int.class,boolean.class).invoke(renderHelper,mc,a,b,left,top,true);}
    private void shot(Minecraft mc,String name){net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.mcDataDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());}
    private void server(String name,Runnable action){ServerWork.enqueue(()->{try{action.run();pass(name);}catch(Throwable e){failed=true;results.add("FAIL: "+name+" :: "+e);IndustrialTechnology.log.error(name,e);}});}
    private void pass(String name){results.add("PASS: "+name);IndustrialTechnology.log.info("WIRELESS CLIENT PASS: "+name);}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private void finish(Minecraft mc){finished=true;try{results.add("Failed: "+(failed?1:0));Files.write(new File(mc.mcDataDir,"wireless-client-integration.txt").toPath(),results,StandardCharsets.UTF_8);}catch(Exception e){IndustrialTechnology.log.error("Wireless client report failed",e);}mc.shutdown();}
}
