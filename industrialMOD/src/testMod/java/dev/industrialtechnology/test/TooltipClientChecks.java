package dev.industrialtechnology.test;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.ScreenShotHelper;
import dev.industrialtechnology.client.*;
import dev.industrialtechnology.core.*;
import dev.industrialtechnology.wireless.*;
import dev.industrialtechnology.quarry.TileQuarry;
import dev.industrialtechnology.machine.MachineContainer;
/** Real NEI coremod/client framebuffer regression, with a wide enough screen for its item panel. */
public final class TooltipClientChecks {
    private static final List<ItemStack> icons=new ArrayList<ItemStack>();
    public static void run(Minecraft mc,Consumer<String> pass)throws Exception{
        Class<?> config=Class.forName("codechicken.nei.NEIClientConfig");
        config.getMethod("setEnabled",boolean.class).invoke(null,true);config.getMethod("setInternalEnabled",boolean.class).invoke(null,true);
        if((Boolean)config.getMethod("isHidden").invoke(null))config.getMethod("toggleBooleanSetting",String.class).invoke(null,"inventory.hidden");
        Display.setDisplayMode(new DisplayMode(1560,900));Method resize=Minecraft.class.getDeclaredMethod("resize",int.class,int.class);resize.setAccessible(true);resize.invoke(mc,1560,900);
        for(net.minecraft.block.Block b:ModRegistry.quarries)icons.add(new ItemStack(b));icons.add(new ItemStack(ModRegistry.loader));
        for(Item item:new Item[]{ModRegistry.yellowStick,ModRegistry.greenStick,ModRegistry.blueStick,ModRegistry.circuit,ModRegistry.drill,ModRegistry.gear,ModRegistry.terminal,ModRegistry.automationGuide})icons.add(new ItemStack(item));
        NBTTagCompound n=new NBTTagCompound();n.setInteger("Tab",2);n.setString("Selected",UUID.randomUUID().toString());n.setString("Name","Logic: Tank");n.setString("Status","ONLINE");n.setInteger("Pages",1);n.setInteger("Minimum",1);n.setString("TargetName","Choose Yellow");n.setInteger("MaxScriptLen",4096);
        GuiWirelessTerminal terminal=new GuiWirelessTerminal(new TerminalContainer(mc.thePlayer));mc.displayGuiScreen(terminal);terminal.receive(n);
        checkTooltipState(terminal);pass.accept("Terminal tooltips restore both GUI light positions before NEI draws in the same frame");
        compareIcons(mc,terminal,15,"terminal-simple");pass.accept("Actual NEI icons retain identical colors while hovering a SIMPLE automation button");
        n.setBoolean("Latched",true);terminal.receive(n);shot(mc,terminal,0,0,"terminal-simple-true.png");n.setBoolean("Latched",false);terminal.receive(n);shot(mc,terminal,0,0,"terminal-simple-false.png");
        verifyStatusRow(mc,terminal);pass.accept("SIMPLE TRUE and FALSE occupy their own rule row clear of CODE and navigation buttons");
        n.setBoolean("CodeMode",true);terminal.receive(n);terminal.editor.set("WHEN FLUID \"water\" >= 200mb\nEVERY 20s\nTHEN PULSE \"WATER\" 5s\nEND");compareIcons(mc,terminal,24,"terminal-code");pass.accept("Actual NEI icon colors stay stable while hovering CODE Save beside EVERY syntax");
        TileQuarry quarry=new TileQuarry.MK3();quarry.setWorldObj(mc.theWorld);quarry.xCoord=24;quarry.yCoord=72;quarry.zCoord=24;
        GuiMachine machine=new GuiMachine(new MachineContainer(mc.thePlayer.inventory,quarry));mc.displayGuiScreen(machine);checkTooltipState(machine);pass.accept("Quarry tooltips preserve GUI lights during the foreground pass");compareIcons(mc,machine,0,"quarry-control");pass.accept("Actual NEI icon colors stay identical while hovering the quarry Start button");
        mc.displayGuiScreen(null);
    }
    private static Method tooltip()throws Exception{Method m=GuiTooltips.class.getDeclaredMethod("drawHoveringText",List.class,int.class,int.class,FontRenderer.class);m.setAccessible(true);return m;}
    private static void checkTooltipState(GuiScreen g)throws Exception{
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{
            RenderHelper.enableGUIStandardItemLighting();GL11.glDisable(GL11.GL_LIGHTING);GL11.glColor4f(.3f,.7f,.2f,1);
            float[] before=lights();tooltip().invoke(g,Arrays.asList("Lighting regression","NEI must see the original lights"),140,150,Minecraft.getMinecraft().fontRenderer);float[] after=lights();
            for(int i=0;i<before.length;i++)check(Math.abs(before[i]-after[i])<.0001f,"Tooltip changed GUI light component "+i);
            check(!GL11.glIsEnabled(GL11.GL_LIGHTING),"Tooltip leaked enabled lighting");FloatBuffer color=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);check(Math.abs(color.get(0)-.3f)<.0001,"Tooltip leaked color");
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();}
    }
    private static float[] lights(){float[] values=new float[8];for(int light=0;light<2;light++){FloatBuffer b=BufferUtils.createFloatBuffer(16);GL11.glGetLight(GL11.GL_LIGHT0+light,GL11.GL_POSITION,b);for(int i=0;i<4;i++)values[light*4+i]=b.get(i);}return values;}
    private static Field field(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
    private static void verifyStatusRow(Minecraft mc,GuiScreen g)throws Exception{
        int left=field(GuiContainer.class,"guiLeft").getInt(g),top=field(GuiContainer.class,"guiTop").getInt(g);int sx=left+240,sy=top+77,sw=mc.fontRenderer.getStringWidth("FALSE"),sh=mc.fontRenderer.FONT_HEIGHT;
        for(Object obj:(List<?>)field(GuiScreen.class,"buttonList").get(g)){GuiButton b=(GuiButton)obj;if(b.visible)check(sx+sw<=b.xPosition||sx>=b.xPosition+b.width||sy+sh<=b.yPosition||sy>=b.yPosition+b.height,"Truth label overlaps button "+b.id);}
    }
    private static void compareIcons(Minecraft mc,GuiContainer g,int button,String prefix)throws Exception{
        GuiButton hover=null;for(Object obj:(List<?>)field(GuiScreen.class,"buttonList").get(g)){GuiButton b=(GuiButton)obj;if(b.id==button)hover=b;}check(hover!=null&&hover.visible,"Hover button unavailable");
        shot(mc,g,0,0,"nei-"+prefix+"-warmup.png");
        BufferedImage first=shot(mc,g,0,0,"nei-"+prefix+"-normal.png"),second=shot(mc,g,hover.xPosition+3,hover.yPosition+3,"nei-"+prefix+"-hover.png");
        Object panel=Class.forName("codechicken.nei.LayoutManager").getField("itemPanel").get(null);Class<?> widget=Class.forName("codechicken.nei.Widget");int px=widget.getField("x").getInt(panel),py=widget.getField("y").getInt(panel),pw=widget.getField("w").getInt(panel);check(pw>=36&&px>g.width/2,"NEI item panel not visible");
        int compared=0,colored=0;Method rect=panel.getClass().getMethod("getSlotRect",int.class);
        for(int i=0;i<icons.size();i++){Object box=rect.invoke(panel,i);Class<?> c=box.getClass();int x=c.getField("x").getInt(box),y=c.getField("y").getInt(box);for(int dy=1;dy<17;dy++)for(int dx=1;dx<17;dx++){
            int xx=(x+dx)*3,yy=(y+dy)*3;check(xx<first.getWidth()&&yy<first.getHeight(),"NEI rect outside framebuffer");int a=first.getRGB(xx,yy),b=second.getRGB(xx,yy);check(a==b,"NEI icon darkened on hover at "+x+","+y+" slot "+i);compared++;int r=(a>>16)&255,green=(a>>8)&255,blue=a&255;if(green>80&&blue>70&&Math.abs(r-green)>15)colored++;
        }}check(compared>=3000&&colored>50,"NEI probe did not include rendered item colors");
    }
    private static BufferedImage shot(Minecraft mc,GuiScreen screen,int mx,int my,String file)throws Exception{
        Class<?> panelType=Class.forName("codechicken.nei.ItemPanel");ArrayList<ItemStack> stable=new ArrayList<ItemStack>(icons);
        panelType.getMethod("updateItemList",ArrayList.class).invoke(null,stable);panelType.getField("items").set(null,stable);
        Object panel=Class.forName("codechicken.nei.LayoutManager").getField("itemPanel").get(null);panelType.getMethod("resize").invoke(panel);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);mc.getFramebuffer().bindFramebuffer(true);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glOrtho(0,screen.width,screen.height,0,1000,3000);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glTranslatef(0,0,-2000);
        try{GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDepthMask(true);GL11.glColorMask(true,true,true,true);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);GL11.glColor4f(1,1,1,1);screen.drawScreen(mx,my,0);ScreenShotHelper.saveScreenshot(mc.mcDataDir,file,mc.displayWidth,mc.displayHeight,mc.getFramebuffer());return ImageIO.read(new File(mc.mcDataDir,"screenshots/"+file));}
        finally{GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);mc.getFramebuffer().unbindFramebuffer();GL11.glPopAttrib();}
    }
    private static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
}
