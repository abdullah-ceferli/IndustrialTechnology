package dev.industrialtechnology.test;
import java.util.*;
import java.lang.reflect.*;
import java.io.File;
import java.nio.*;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.network.play.client.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.*;
import net.minecraft.nbt.*;
import org.lwjgl.input.Keyboard;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.client.*;
import dev.industrialtechnology.wireless.*;
public final class CodeClientTest {
    private final List<String> results=Collections.synchronizedList(new ArrayList<String>());private final ClientTestProxy renderHelper=new ClientTestProxy();
    private int phase,step,wait=30,startup;private boolean finished;private volatile boolean failed;private static long deadline;
    private static final String WATER="WHEN FLUID \"water\" >= 200mb\nTHEN PULSE \"Pump\" 7s\nEND",BAD="WHEN FLUID \"unknown_liquid\" >= 200mb\nTHEN PULSE \"Pump\" 7s\nEND";
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END||finished||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();
        try{if(failed){finish(mc);return;}if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=3;mc.gameSettings.renderDistanceChunks=3;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;WorldSettings settings=new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);settings.func_82750_a("2;7;1;");mc.launchIntegratedServer("code-client","Automation Code",settings);phase=1;wait=60;return;}
            if(mc.thePlayer==null||mc.theWorld==null){if(++startup>60)throw new AssertionError("Join timeout");wait=20;return;}
            if(phase==1){mc.displayGuiScreen(null);ServerWork.enqueue(()->setup());phase=2;wait=60;return;}
            run(mc);step++;wait=step==27?115:25;
        }catch(Throwable e){failed=true;results.add("FAIL: code client step "+step+" :: "+e);IndustrialTechnology.log.error("CODE CLIENT FAILED",e);net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.mcDataDir,"code-error.png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());finish(mc);}}
    private static WorldServer world(){return MinecraftServer.getServer().worldServerForDimension(0);}
    private static EntityPlayerMP player(){return(EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);}
    private static WirelessEndpoint device(WirelessEndpoint.Kind kind,int x){for(WirelessEndpoint e:WirelessData.get(world()).devices.values())if(e.kind==kind&&e.x==x)return e;throw new AssertionError("Device missing");}
    private void setup(){WorldServer w=world();EntityPlayerMP p=player();for(int x=20;x<=35;x++)for(int z=20;z<=27;z++)w.setBlock(x,71,z,Blocks.stone,0,3);w.setBlock(24,72,24,WirelessTestFixtures.tanks,0,3);WirelessTestFixtures.Tanks tanks=(WirelessTestFixtures.Tanks)w.getTileEntity(24,72,24);tanks.water.setFluid(new net.minecraftforge.fluids.FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER,100));tanks.lava.setFluid(null);w.setBlock(28,72,24,Blocks.chest,0,3);w.setBlock(32,72,24,Blocks.redstone_lamp,0,3);
        p.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));p.inventory.setInventorySlotContents(1,new ItemStack(ModRegistry.blueStick,8));p.inventory.setInventorySlotContents(2,new ItemStack(ModRegistry.yellowStick,8));p.inventory.setInventorySlotContents(3,new ItemStack(ModRegistry.automationGuide));p.inventory.setInventorySlotContents(4,new ItemStack(Blocks.stone,32));p.inventory.currentItem=0;WirelessAccess.terminal(p,p.getHeldItem(),true);p.capabilities.isFlying=true;p.sendPlayerAbilities();p.setSneaking(false);p.setPositionAndUpdate(28.5,73,22.5);p.inventory.markDirty();}
    private void run(Minecraft mc)throws Exception{switch(step){
        case 0:{GuiScreen vanilla=(GuiScreen)method(ClientTestProxy.class,"vanillaInventory",Minecraft.class).invoke(null,mc);render(mc,vanilla,"code-vanilla-before.png",false);hold(mc,1);place(mc,24,72,24,2);break;}
        case 1:place(mc,28,72,24,2);break;
        case 2:hold(mc,2);place(mc,32,72,24,2);break;
        case 3:server("Physical Blue tank and chest torches and Yellow control link before Code use",()->{
            WirelessEndpoint control=device(WirelessEndpoint.Kind.CONTROL,32);control.name="Pump";WirelessEndpoint old=device(WirelessEndpoint.Kind.AUTOMATION,28);old.rules[0].enabled=true;old.rules[0].item=new ItemStack(Items.coal);old.rules[0].minimum=1;old.rules[0].target=control.id;WirelessData.get(world()).markDirty();});hold(mc,0);mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem());break;
        case 4:click(gui(mc),3);break;
        case 5:click(gui(mc),23);break;
        case 6:{GuiWirelessTerminal g=gui(mc);check(g.state.getBoolean("CodeMode")&&g.editor.height>=130,"Code editor not active/large");for(Object v:buttons(g)){GuiButton b=(GuiButton)v;if(b.id>=12&&b.id<=17)check(!b.visible,"old controls visible");}check(g.terminal.codeLayout&&((net.minecraft.inventory.Slot)g.terminal.inventorySlots.get(36)).yDisplayPosition<0,"ghost slot still editable");pass("CODE replaces old rule widgets with a real large multiline editor");mouse(g,140,80);type(g,WATER);check(g.editor.model.lines().length==3&&g.editor.model.text().equals(WATER),"multiline typing failed");pass("Actual editor typing Enter arrows Backspace and Delete preserves three code lines");key(g,'x',Keyboard.KEY_X);key(g,'\b',Keyboard.KEY_BACK);key(g,(char)0,Keyboard.KEY_HOME);key(g,(char)0,Keyboard.KEY_DELETE);key(g,'E',Keyboard.KEY_E);key(g,(char)0,Keyboard.KEY_END);check(g.editor.model.text().equals(WATER),"Delete or cursor editing failed");break;}
        case 7:{GuiWirelessTerminal g=gui(mc);mouse(g,148,72);int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);method(GuiWirelessTerminal.class,"mouseClickMove",int.class,int.class,int.class,long.class).invoke(g,left+147+mc.fontRenderer.getStringWidth("WHEN"),top+72,0,50L);check(g.editor.model.selected().equals("WHEN"),"mouse drag selection failed");method(GuiWirelessTerminal.class,"mouseMovedOrUp",int.class,int.class,int.class).invoke(g,left+147,top+72,0);ctrl(g,Keyboard.KEY_END);ctrl(g,Keyboard.KEY_A);ctrl(g,Keyboard.KEY_C);check(GuiScreen.getClipboardString().equals(WATER),"copy failed");ctrl(g,Keyboard.KEY_X);check(g.editor.model.text().isEmpty(),"cut failed");ctrl(g,Keyboard.KEY_V);ctrl(g,Keyboard.KEY_A);key(g,'x',Keyboard.KEY_X);ctrl(g,Keyboard.KEY_A);ctrl(g,Keyboard.KEY_V);check(g.editor.model.text().equals(WATER),"paste failed");pass("Ctrl selection copy paste uses the system clipboard without activating game hotkeys");click(g,24);break;}
        case 8:{GuiWirelessTerminal g=gui(mc);check(g.state.getInteger("ScriptRevision")>0&&g.state.getString("ScriptError").isEmpty()&&!g.editor.dirty,"Save not acknowledged");pass("Save sends the real bounded upload and acknowledges a compiled program");render(mc,g,"code-editor-saved.png",false);render(mc,g,"code-editor-tint.png",true);compare(mc,g,"code-editor-saved.png","code-editor-tint.png");pass("Editor panels syntax colors and caller OpenGL tint stay stable");click(g,26);break;}
        case 9:check(gui(mc).state.getBoolean("ScriptEnabled"),"Enable failed");server("100mb water remains false before the actual fluid threshold",()->check(!device(WirelessEndpoint.Kind.CONTROL,32).powered,"100mb fired"));ServerWork.enqueue(()->((WirelessTestFixtures.Tanks)world().getTileEntity(24,72,24)).water.fill(new net.minecraftforge.fluids.FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER,100),true));break;
        case 10:server("GUI-saved fluid script starts real power 15 and a seven-second deadline at 200mb",()->{WirelessEndpoint e=device(WirelessEndpoint.Kind.CONTROL,32);deadline=e.pulseEnd;check(WirelessAccess.relay(e).signal()&&deadline-world().getTotalWorldTime()>0&&deadline-world().getTotalWorldTime()<=140,"fluid pulse failed");});render(mc,gui(mc),"code-fluid-running.png",false);click(gui(mc),24);break;
        case 11:server("Saving unchanged working code preserves its latch and active pulse deadline",()->check(device(WirelessEndpoint.Kind.CONTROL,32).pulseEnd==deadline,"save extended pulse"));replace(gui(mc),BAD);click(gui(mc),24);break;
        case 12:{GuiWirelessTerminal g=gui(mc);check(g.state.getInteger("ScriptErrorLine")==1&&g.state.getString("ScriptError").contains("Unknown fluid")&&g.editor.model.text().equals(BAD),"bad draft/error lost");server("Invalid GUI save keeps the prior working script",()->check(device(WirelessEndpoint.Kind.AUTOMATION,24).script.equals(WATER),"invalid save replaced source"));render(mc,g,"code-invalid-fluid.png",false);pass("Line errors preserve the player's invalid draft and last working program");click(g,25);break;}
        case 13:{GuiWirelessTerminal g=gui(mc);check(g.guide!=null&&g.guide.pages.size()>15,"Help book missing");for(GuiAutomationGuide.Page p:g.guide.pages){check(p.lines.size()<=13,"book page overflow");for(String line:p.lines)check(mc.fontRenderer.getStringWidth(line)<=116,"book line overflow: "+line);}render(mc,g.guide,"automation-help-book.png",false);pass("Help uses classic cream book pages and every page fits the actual Minecraft font");bookClick(g.guide,1);break;}
        case 14:check(gui(mc).guide.page==1,"page next failed");bookClick(gui(mc).guide,2);check(gui(mc).guide.page==0,"page back failed");pass("Book page arrows navigate real pages");bookClick(gui(mc).guide,0);break;
        case 15:check(gui(mc).guide==null&&gui(mc).editor.model.text().equals(BAD),"Help discarded draft/closed terminal");pass("Closing Help preserves the code draft and live terminal container");replace(gui(mc),WATER);click(gui(mc),24);break;
        case 16:click(gui(mc),26);break;
        case 17:check(!gui(mc).state.getBoolean("ScriptEnabled"),"Disable failed");server("Seven-second fluid pulse expires even while editing invalid drafts or viewing Help",()->check(!device(WirelessEndpoint.Kind.CONTROL,32).powered,"fluid pulse repeated or remained ON"));click(gui(mc),22);break;
        case 18:check(!gui(mc).state.getBoolean("CodeMode"),"SIMPLE switch failed");for(Object v:buttons(gui(mc))){GuiButton b=(GuiButton)v;if(b.id==16)check(b.visible,"simple controls missing");}pass("SIMPLE returns without erasing saved code");mouse(gui(mc),20,66);break;
        case 19:check(gui(mc).state.getBoolean("RuleEnabled")&&gui(mc).terminal.ghost.getStackInSlot(0).getItem()==Items.coal,"old simple rule erased");pass("Previously saved SIMPLE coal rules and ghost items remain usable");click(gui(mc),23);break;
        case 20:replace(gui(mc),"WHEN ITEM \"minecraft:stone\" >= 1\nTHEN PULSE \"Pump\" 5s\nEND");click(gui(mc),24);break;
        case 21:click(gui(mc),26);break;
        case 22:ServerWork.enqueue(()->((net.minecraft.tileentity.TileEntityChest)world().getTileEntity(28,72,24)).setInventorySlotContents(0,new ItemStack(Blocks.stone)));break;
        case 23:server("GUI item code uses real chest insertion and starts a five-second Yellow pulse",()->{WirelessEndpoint e=device(WirelessEndpoint.Kind.CONTROL,32);check(e.powered&&e.pulseEnd-world().getTotalWorldTime()<=100&&e.pulseEnd-world().getTotalWorldTime()>0,"item code failed");});render(mc,gui(mc),"code-item-running.png",false);break;
        case 24:mouse(gui(mc),20,52);break;
        case 25:click(gui(mc),23);break;
        case 26:check(gui(mc).editor.model.text().equals(WATER)&&!gui(mc).state.getBoolean("ScriptEnabled"),"device/code mode persistence lost");pass("Changing Blue devices restores each independent saved program and mode");break;
        case 27:server("Five-second item code turns OFF and does not repeat while stone stays present",()->check(!device(WirelessEndpoint.Kind.CONTROL,32).powered&&device(WirelessEndpoint.Kind.AUTOMATION,28).scriptRules.get(0).latched,"item pulse repeated"));replace(gui(mc),WATER+"\n# scrolling\n# one\n# two\n# three\n# four\n# five\n# six\n# seven\n# eight\n# nine\n# ten\n# eleven\n# twelve\n# thirteen\n# fourteen");gui(mc).editor.scroll(5);check(gui(mc).editor.scroll>0,"editor failed to scroll");render(mc,gui(mc),"code-editor-scroll.png",false);pass("Multiline scrolling and line numbers handle programs longer than the visible editor");mc.thePlayer.closeScreen();hold(mc,3);break;
        case 28:mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem());break;
        case 29:check(mc.currentScreen instanceof GuiAutomationGuide,"held book right-click failed");render(mc,mc.currentScreen,"automation-guide-item.png",false);pass("Craftable leather guide item opens an ordinary Minecraft-style book on right-click");bookClick((GuiAutomationGuide)mc.currentScreen,1);break;
        case 30:check(((GuiAutomationGuide)mc.currentScreen).page==1,"item book page navigation failed");bookClick((GuiAutomationGuide)mc.currentScreen,0);break;
        case 31:{GuiScreen vanilla=(GuiScreen)method(ClientTestProxy.class,"vanillaInventory",Minecraft.class).invoke(null,mc);render(mc,vanilla,"code-vanilla-after.png",false);method(ClientTestProxy.class,"comparePanel",Minecraft.class,String.class,String.class,int.class,int.class,boolean.class).invoke(renderHelper,mc,"code-vanilla-before.png","code-vanilla-after.png",72,37,false);pass("Editor and book rendering restore vanilla GUI colors and player items");finish(mc);break;}
    }}
    private static GuiWirelessTerminal gui(Minecraft mc){check(mc.currentScreen instanceof GuiWirelessTerminal,"Terminal missing: "+mc.currentScreen);return(GuiWirelessTerminal)mc.currentScreen;}
    private static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
    private static Method method(Class<?> c,String name,Class<?>...args)throws Exception{Method m=c.getDeclaredMethod(name,args);m.setAccessible(true);return m;}
    private static List<?> buttons(GuiScreen g)throws Exception{return(List<?>)field(GuiScreen.class,"buttonList").get(g);}
    private static void click(GuiWirelessTerminal g,int id)throws Exception{for(Object v:buttons(g)){GuiButton b=(GuiButton)v;if(b.id==id){check(b.visible&&b.enabled,"Button unavailable "+id);method(GuiWirelessTerminal.class,"actionPerformed",GuiButton.class).invoke(g,b);return;}}throw new AssertionError("Button missing "+id);}
    private static void bookClick(GuiAutomationGuide g,int id)throws Exception{for(Object v:buttons(g)){GuiButton b=(GuiButton)v;if(b.id==id){check(b.visible&&b.enabled,"Book button unavailable");method(GuiAutomationGuide.class,"actionPerformed",GuiButton.class).invoke(g,b);return;}}throw new AssertionError("Book button missing");}
    private static void mouse(GuiWirelessTerminal g,int x,int y)throws Exception{int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);method(GuiWirelessTerminal.class,"mouseClicked",int.class,int.class,int.class).invoke(g,left+x,top+y,0);}
    private static void key(GuiWirelessTerminal g,char c,int key)throws Exception{method(GuiWirelessTerminal.class,"keyTyped",char.class,int.class).invoke(g,c,key);}
    private static void ctrl(GuiWirelessTerminal g,int key)throws Exception{ByteBuffer buffer=(ByteBuffer)field(Keyboard.class,"keyDownBuffer").get(null);byte previous=buffer.get(Keyboard.KEY_LCONTROL);buffer.put(Keyboard.KEY_LCONTROL,(byte)1);try{key(g,(char)0,key);}finally{buffer.put(Keyboard.KEY_LCONTROL,previous);}}
    private static void type(GuiWirelessTerminal g,String text)throws Exception{for(int i=0;i<text.length();i++){char c=text.charAt(i);key(g,c,c=='\n'?Keyboard.KEY_RETURN:0);}}
    private static void replace(GuiWirelessTerminal g,String text)throws Exception{g.editor.focused=true;ctrl(g,Keyboard.KEY_A);GuiScreen.setClipboardString(text);ctrl(g,Keyboard.KEY_V);}
    private static void hold(Minecraft mc,int slot){mc.thePlayer.inventory.currentItem=slot;mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));}
    private static void place(Minecraft mc,int x,int y,int z,int side){mc.thePlayer.setSneaking(false);mc.playerController.onPlayerRightClick(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem(),x,y,z,side,net.minecraft.util.Vec3.createVectorHelper(x+.5,y+.5,z+.5));}
    private void render(Minecraft mc,GuiScreen g,String name,boolean tint)throws Exception{method(ClientTestProxy.class,"renderShot",Minecraft.class,GuiScreen.class,String.class,boolean.class).invoke(renderHelper,mc,g,name,tint);}
    private void compare(Minecraft mc,GuiWirelessTerminal g,String a,String b)throws Exception{int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);method(ClientTestProxy.class,"comparePanel",Minecraft.class,String.class,String.class,int.class,int.class,boolean.class).invoke(renderHelper,mc,a,b,left,top,true);}
    private void pass(String message){results.add("PASS: "+message);IndustrialTechnology.log.info("CODE CLIENT PASS: "+message);}
    private void server(String message,Runnable action){ServerWork.enqueue(()->{try{action.run();pass(message);}catch(Throwable e){failed=true;results.add("FAIL: "+message+" :: "+e);IndustrialTechnology.log.error(message,e);}});}
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    private void finish(Minecraft mc){finished=true;try{results.add("Failed: "+(failed?1:0));Files.write(new File(mc.mcDataDir,"code-client-integration.txt").toPath(),results,StandardCharsets.UTF_8);}catch(Exception e){IndustrialTechnology.log.error("Code report failed",e);}mc.shutdown();}
}
