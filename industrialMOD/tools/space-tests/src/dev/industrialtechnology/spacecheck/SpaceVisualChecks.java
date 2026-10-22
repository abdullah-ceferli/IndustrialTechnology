package dev.industrialtechnology.spacecheck;

import java.util.*;import java.io.*;import java.nio.*;import java.nio.file.*;import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;import java.awt.image.BufferedImage;
import cpw.mods.fml.common.*;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.*;import net.minecraft.client.entity.EntityOtherPlayerMP;import net.minecraft.client.gui.*;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.nbt.*;import net.minecraft.world.*;import net.minecraft.server.MinecraftServer;import net.minecraft.util.*;import net.minecraft.block.Block;
import net.minecraftforge.fluids.*;import org.lwjgl.opengl.GL11;import org.lwjgl.BufferUtils;import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.*;import dev.industrialtechnology.space.*;import dev.industrialtechnology.client.space.*;

/** Test-only real client gallery. Never bundled in the gameplay mod. */
public final class SpaceVisualChecks {
    private final List<String> report=Collections.synchronizedList(new ArrayList<String>());private int phase,wait=30,timeout,gui;private boolean failed,done;private net.minecraft.entity.EntityLivingBase camera;
    private static final int[] GUI_KINDS={0,1,2,3,4,5,6,7,8,9,10};
    public void init(){FMLCommonHandler.instance().bus().register(this);}
    private void check(boolean b,String label){if(!b)throw new AssertionError(label);report.add("PASS: "+label);}
    private void server(Runnable action){ServerWork.enqueue(()->{try{action.run();}catch(Throwable t){failed=true;report.add("FAIL: fixture :: "+t);IndustrialTechnology.log.error("Visual fixture",t);}});}
    private static EntityPlayerMP player(){return (EntityPlayerMP)MinecraftServer.getServer().getConfigurationManager().playerEntityList.get(0);}
    private static WorldServer world(){return (WorldServer)player().worldObj;}
    private static int x(int kind){return 4+kind%6*5;}private static int z(int kind){return 8+kind/6*8;}
    private void position(double x,double y,double z,float yaw,float pitch){Minecraft mc=Minecraft.getMinecraft();mc.renderViewEntity=mc.thePlayer;server(()->player().playerNetServerHandler.setPlayerLocation(x,y,z,yaw,pitch));}
    private void view(double x,double y,double z,float yaw,float pitch){Minecraft mc=Minecraft.getMinecraft();camera=new EntityOtherPlayerMP(mc.theWorld,mc.thePlayer.getGameProfile());camera.setPositionAndRotation(x,y,z,yaw,pitch);camera.prevPosX=camera.lastTickPosX=x;camera.prevPosY=camera.lastTickPosY=y;camera.prevPosZ=camera.lastTickPosZ=z;camera.prevRotationYaw=yaw;camera.prevRotationPitch=pitch;mc.renderViewEntity=camera;}
    private void shot(String name){Minecraft mc=Minecraft.getMinecraft();ScreenShotHelper.saveScreenshot(mc.mcDataDir,"visual-"+name+".png",mc.displayWidth,mc.displayHeight,mc.getFramebuffer());}
    private void state(String label){FloatBuffer c=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,c);check(c.get(0)>.99f&&c.get(1)>.99f&&c.get(2)>.99f,label+" leaves white OpenGL color");check(GL11.glGetError()==GL11.GL_NO_ERROR,label+" leaves no OpenGL error");}
    private void resources()throws Exception{
        Minecraft mc=Minecraft.getMinecraft();int count=0;String[] faces={"","_side","_top","_bottom"};
        for(String n:SpaceRegistry.MACHINES)for(String suffix:faces){String full=n+suffix;try(InputStream in=mc.getResourceManager().getResource(new ResourceLocation("industrialtech:textures/blocks/"+full+".png")).getInputStream()){BufferedImage im=ImageIO.read(in);check(im!=null&&im.getWidth()==16&&im.getHeight()==16,"Loaded 16px face "+full);count++;}}
        for(int tier=1;tier<=3;tier++)for(int layer=1;layer<=2;layer++)try(InputStream in=mc.getResourceManager().getResource(new ResourceLocation("industrialtech:textures/armor/suit_"+tier+"_"+layer+".png")).getInputStream()){BufferedImage im=ImageIO.read(in);check(im.getWidth()==64&&im.getHeight()==64,"Armor UV sheet tier "+tier+" layer "+layer+" has dedicated model area");}
        for(Block b:SpaceRegistry.machines){BlockSpaceMachine m=(BlockSpaceMachine)b;for(int facing=2;facing<=5;facing++){check(m.getIcon(facing,facing).getIconName().equals("industrialtech:"+SpaceRegistry.MACHINES[m.kind]),"Front matches facing "+facing+" for "+SpaceRegistry.MACHINES[m.kind]);check(!m.getIcon(1,facing).getIconName().equals(m.getIcon(0,facing).getIconName()),"Top and underside differ for "+SpaceRegistry.MACHINES[m.kind]+" / "+facing);}check(BlockSpaceMachine.front(0)==3&&m.getIcon(2,0)==m.getIcon(2,2),"Legacy metadata keeps visible front for "+SpaceRegistry.MACHINES[m.kind]);}
    }
    private void fixture(){EntityPlayerMP p=player();WorldServer w=world();w.setWorldTime(6000);w.getGameRules().setOrCreateGameRule("doDaylightCycle","false");w.getGameRules().setOrCreateGameRule("doMobSpawning","false");
        for(int xx=-6;xx<=48;xx++)for(int zz=-4;zz<=44;zz++)w.setBlock(xx,70,zz,SpaceRegistry.hull,0,3);
        for(int k=0;k<11;k++){w.setBlock(x(k),71,z(k),SpaceRegistry.machines[k],3,3);TileSpaceMachine t=(TileSpaceMachine)w.getTileEntity(x(k),71,z(k));t.owner=p.getUniqueID().toString();t.energy=1000000;t.tanks[0].fill(new FluidStack(k==3?SpaceRegistry.crude:SpaceRegistry.air,50000),true);t.tanks[1].fill(new FluidStack(k==8?SpaceRegistry.lox:SpaceRegistry.kerosene,30000),true);if(k==0){t.items[0]=new ItemStack(SpaceRegistry.hdd);t.items[0].setTagCompound(new NBTTagCompound());}if(k==2)for(int i=0;i<11;i++)t.items[i]=new ItemStack(SpaceRegistry.parts[i]);w.markBlockForUpdate(x(k),71,z(k));}
        // Actual placement hook in each direction must preserve tile content.
        for(int rot=0;rot<4;rot++){w.setBlock(40,71,rot*2,SpaceRegistry.machines[7],0,3);TileSpaceMachine t=(TileSpaceMachine)w.getTileEntity(40,71,rot*2);t.items[0]=new ItemStack(SpaceRegistry.canister);p.rotationYaw=rot*90;SpaceRegistry.machines[7].onBlockPlacedBy(w,40,71,rot*2,p,new ItemStack(SpaceRegistry.machines[7]));check(w.getBlockMetadata(40,71,rot*2)==new int[]{2,5,3,4}[rot],"Placement direction "+rot+" faces player");check(w.getTileEntity(40,71,rot*2)==t&&t.items[0]!=null,"Facing update preserves tile and item "+rot);}
        TileSpaceMachine pad=SpaceChecks.launchpad(w,32,70,32);pad.items[0]=SpaceChecks.rocket();pad.owner=p.getUniqueID().toString();w.markBlockForUpdate(32,70,32);
        Block[] geo={SpaceRegistry.moonRock,SpaceRegistry.regolith,SpaceRegistry.basalt,SpaceRegistry.breccia,SpaceRegistry.lunarDust,SpaceRegistry.marsRock,SpaceRegistry.marsDust,SpaceRegistry.marsLayer,SpaceRegistry.ice};
        for(int k=0;k<geo.length;k++)for(int dy=0;dy<3;dy++)for(int dx=0;dx<3;dx++)w.setBlock(2+k*4+dx,71+dy,24,geo[k],0,3);
        w.setBlock(10,71,20,SpaceRegistry.door,0,3);w.setBlock(12,71,20,SpaceRegistry.glass,0,3);w.setBlock(13,71,20,SpaceRegistry.glass,0,3);w.setBlock(15,71,20,SpaceRegistry.lamp,0,3);w.setBlock(16,71,20,Blocks.redstone_block,0,3);w.setBlock(18,71,20,ModRegistry.quarries[0],3,3);w.setBlock(20,71,20,ModRegistry.loader,0,3);
        p.setGameType(WorldSettings.GameType.CREATIVE);p.playerNetServerHandler.setPlayerLocation(16,72,21,180,10);
        check(w.getBlock(10,72,20)==SpaceRegistry.door,"Two-part airlock door remains intact");
    }
    private void suits(){Minecraft mc=Minecraft.getMinecraft();for(int tier=0;tier<3;tier++)for(int back=0;back<2;back++){EntityOtherPlayerMP e=new EntityOtherPlayerMP(mc.theWorld,new GameProfile(UUID.nameUUIDFromBytes(("suit"+tier+back).getBytes(StandardCharsets.UTF_8)),"Tier"+(tier+1)));e.setPositionAndRotation(5+tier*4+back*1.5,71,34,back==0?0:180,0);e.renderYawOffset=e.rotationYaw;e.rotationYawHead=e.rotationYaw;e.prevRenderYawOffset=e.renderYawOffset;e.prevRotationYaw=e.rotationYaw;for(int slot=0;slot<4;slot++)e.inventory.armorInventory[3-slot]=new ItemStack(SpaceRegistry.suits[tier][slot]);mc.theWorld.addEntityToWorld(-100-tier*2-back,e);}}
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END||done||--wait>0)return;Minecraft mc=Minecraft.getMinecraft();try{
        if(failed){finish();return;}
        if(phase==0){if(!(mc.currentScreen instanceof GuiMainMenu)){wait=20;return;}mc.gameSettings.guiScale=2;mc.gameSettings.renderDistanceChunks=4;mc.gameSettings.pauseOnLostFocus=false;mc.gameSettings.showInventoryAchievementHint=false;mc.gameSettings.fboEnable=true;WorldSettings s=new WorldSettings(7741,WorldSettings.GameType.CREATIVE,false,false,WorldType.FLAT);s.func_82750_a("2;7;1;");mc.launchIntegratedServer("visual-gallery","Space Art QA 2.0.1",s);phase++;wait=80;return;}
        if(mc.theWorld==null||mc.thePlayer==null){if(++timeout>120)throw new AssertionError("Join timeout");wait=10;return;}
        if(phase==1){mc.displayGuiScreen(null);resources();server(this::fixture);phase++;wait=100;return;}
        if(phase==2){BlockSpaceMachine old=(BlockSpaceMachine)SpaceRegistry.machines[7];check(old.getIcon(mc.theWorld,40,71,0,2)==old.getIcon(2,2),"World-facing overload matches placed orientation");view(18,78,25,180,23);mc.gameSettings.hideGUI=true;phase++;wait=35;return;}
        if(phase==3){shot("machine-gallery");state("Equipment gallery");view(6.5,72.2,12.8,180,16);phase++;wait=25;return;}
        if(phase==4){shot("research-computer-telescope");view(16,72.3,21.3,180,15);phase++;wait=25;return;}
        if(phase==5){shot("air-machines");view(45,76,43,135,4);phase++;wait=25;return;}
        if(phase==6){shot("rocket-pad");if(Boolean.getBoolean("industrialtech.spaceModelOnly")){view(36,71.5,36,135,10);phase=12;wait=25;return;}view(20,75,31,180,12);phase++;wait=25;return;}
        if(phase==7){shot("geological-samples");suits();view(10,71,40,180,7);phase++;wait=30;return;}
        if(phase==8){shot("suit-tiers-front-back");state("Three suit tiers");mc.gameSettings.hideGUI=false;mc.renderViewEntity=mc.thePlayer;gui=0;openGui();phase++;wait=35;return;}
        if(phase==9){if(!(mc.currentScreen instanceof GuiSpace))throw new AssertionError("Space GUI "+gui+" missing");GuiSpace g=(GuiSpace)mc.currentScreen;
            // Render actual button hover and a normal NEI item tooltip in this frame.
            ScaledResolution scaled=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);int left=(scaled.getScaledWidth()-320)/2,top=(scaled.getScaledHeight()-238)/2;compareNei(mc,g,left+225,top+114);state("GUI "+SpaceRegistry.MACHINES[gui]);shot("gui-"+SpaceRegistry.MACHINES[gui]);check(g.container.tile.kind==gui,"Correct synced container for "+SpaceRegistry.MACHINES[gui]);mc.thePlayer.closeScreen();if(++gui<11){openGui();wait=30;return;}mc.displayGuiScreen(new Catalog());phase++;wait=35;return;}
        if(phase==10){shot("item-catalogue");state("Item catalogue");check(mc.currentScreen instanceof Catalog,"All space block and item inventory renders complete");mc.displayGuiScreen(null);mc.refreshResources();mc.gameSettings.hideGUI=true;view(45,76,43,135,4);phase++;wait=40;return;}
    if(phase==12){shot("landing-struts-closeup");state("Rocket landing struts");finish();return;}
        if(phase==11){shot("rocket-after-resource-reload");state("Equipment after resource reload");check(mc.theWorld.getBlock(32,70,32)==SpaceRegistry.machines[8],"Resource reload keeps the launch controller and textured rocket");finish();}
    }catch(Throwable t){failed=true;report.add("FAIL: phase "+phase+" GUI "+gui+" :: "+t);IndustrialTechnology.log.error("Space visual QA",t);shot("error");finish();}}
    private void compareNei(Minecraft mc,GuiSpace g,int mx,int my)throws Exception {
        guiShot(mc,g,0,0,"warmup");BufferedImage normal=guiShot(mc,g,0,0,"normal"),hover=guiShot(mc,g,mx,my,"hover");
        Object panel=Class.forName("codechicken.nei.LayoutManager").getField("itemPanel").get(null);java.lang.reflect.Method rect=panel.getClass().getMethod("getSlotRect",int.class);int colored=0;
        for(int i=0;i<12;i++){Object box=rect.invoke(panel,i);Class<?> c=box.getClass();int x=c.getField("x").getInt(box),y=c.getField("y").getInt(box);for(int dy=1;dy<17;dy++)for(int dx=1;dx<17;dx++){int a=normal.getRGB((x+dx)*2,(y+dy)*2),b=hover.getRGB((x+dx)*2,(y+dy)*2);if(a!=b)throw new AssertionError("NEI hover changed item "+i+" pixel "+dx+","+dy);int r=a>>16&255,green=a>>8&255,blue=a&255;if(green>70&&blue>60&&Math.abs(r-green)>10)colored++;}}
        check(colored>20,"Actual NEI item pixels are identical under button hover for "+SpaceRegistry.MACHINES[gui]);
    }
    private BufferedImage guiShot(Minecraft mc,GuiScreen screen,int mx,int my,String suffix)throws Exception {
        ArrayList<ItemStack> stable=new ArrayList<ItemStack>();for(Block b:ModRegistry.quarries)stable.add(new ItemStack(b));stable.add(new ItemStack(ModRegistry.loader));for(int i=0;i<8;i++)stable.add(new ItemStack(SpaceRegistry.machines[i]));
        Class<?> panelType=Class.forName("codechicken.nei.ItemPanel");panelType.getMethod("updateItemList",ArrayList.class).invoke(null,stable);panelType.getField("items").set(null,stable);Object panel=Class.forName("codechicken.nei.LayoutManager").getField("itemPanel").get(null);panelType.getMethod("resize").invoke(panel);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);mc.getFramebuffer().bindFramebuffer(true);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glOrtho(0,screen.width,screen.height,0,1000,3000);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glTranslatef(0,0,-2000);
        String name="visual-nei-"+SpaceRegistry.MACHINES[gui]+"-"+suffix+".png";
        try{GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDepthMask(true);GL11.glColorMask(true,true,true,true);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);GL11.glColor4f(1,1,1,1);screen.drawScreen(mx,my,0);ScreenShotHelper.saveScreenshot(mc.mcDataDir,name,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());return ImageIO.read(new File(mc.mcDataDir,"screenshots/"+name));}
        finally{GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);mc.getFramebuffer().unbindFramebuffer();GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}
    }
    private void openGui(){int k=GUI_KINDS[gui];position(x(k)+.5,72,z(k)+2,180,10);server(()->player().openGui(IndustrialTechnology.instance,2,world(),x(k),71,z(k)));}
    private void finish(){if(done)return;done=true;report.add("Failed: "+(failed?1:0));try{Files.write(new File(Minecraft.getMinecraft().mcDataDir,"space-visual-report.txt").toPath(),String.join("\n",report).getBytes(StandardCharsets.UTF_8));}catch(IOException ex){IndustrialTechnology.log.error("Visual report",ex);}Minecraft.getMinecraft().shutdown();}
    private static final class Catalog extends GuiScreen {
        private final List<ItemStack> items=new ArrayList<ItemStack>();private final RenderItem renderer=new RenderItem();
        Catalog(){for(Block b:SpaceRegistry.machines)items.add(new ItemStack(b));for(Item i:SpaceRegistry.parts)items.add(new ItemStack(i));for(Item i:new Item[]{SpaceRegistry.hdd,SpaceRegistry.rocket,SpaceRegistry.canister,SpaceRegistry.airFilter,SpaceRegistry.steel,SpaceRegistry.titanium,SpaceRegistry.alloy})items.add(new ItemStack(i));for(ItemSpaceSuit[] tier:SpaceRegistry.suits)for(Item i:tier)items.add(new ItemStack(i));for(Block b:new Block[]{SpaceRegistry.launchpad,SpaceRegistry.hull,SpaceRegistry.glass,SpaceRegistry.door,SpaceRegistry.lamp,SpaceRegistry.moonRock,SpaceRegistry.regolith,SpaceRegistry.basalt,SpaceRegistry.breccia,SpaceRegistry.lunarDust,SpaceRegistry.marsRock,SpaceRegistry.marsDust,SpaceRegistry.marsLayer,SpaceRegistry.ice})items.add(new ItemStack(b));}
        @Override public boolean doesGuiPauseGame(){return false;}
        @Override public void drawScreen(int mx,int my,float partial){drawRect(0,0,width,height,0xff252c30);fontRendererObj.drawString("SPACE EQUIPMENT / ORIGINAL PIXEL ART",12,10,0xe3e5d8);GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{RenderHelper.enableGUIStandardItemLighting();for(int i=0;i<items.size();i++){int x=12+(i%12)*38,y=32+(i/12)*48;drawRect(x-2,y-2,x+28,y+28,0xff384247);GL11.glColor4f(1,1,1,1);renderer.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),items.get(i),x+5,y+3);RenderHelper.disableStandardItemLighting();fontRendererObj.drawString(String.valueOf(i+1),x+1,y+31,0x9ca9a4);RenderHelper.enableGUIStandardItemLighting();}}finally{GL11.glPopMatrix();GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}}
    }
}
