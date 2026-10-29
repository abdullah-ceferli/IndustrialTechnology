package dev.industrialtechnology.client;
import dev.industrialtechnology.steel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.*;

/** Original HBM blast-furnace layout with an HBM electrical meter, plus retained buffers. */
public final class GuiSteelFactory extends GuiTooltips {
    public static final ResourceLocation PANEL=new ResourceLocation("hbm","textures/gui/GUIDiFurnace.png");
    public static final ResourceLocation ELECTRIC=new ResourceLocation("hbm","textures/gui/processing/gui_electric_furnace.png");
    public final SteelContainer steel;
    public GuiSteelFactory(SteelContainer c){super(c);steel=c;xSize=204;ySize=196;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
        int x=guiLeft,y=guiTop;SteelGuiArt.frame(x,y,xSize,ySize);
        for(Object o:inventorySlots.inventorySlots){net.minecraft.inventory.Slot slot=(net.minecraft.inventory.Slot)o;if(slot.inventory!=steel.factory)SteelGuiArt.slot(x+slot.xDisplayPosition,y+slot.yDisplayPosition);}
        if(SteelRegistry.hasHBM)SteelGuiArt.process(x,y,steel.shownPower,steel.shownProgress,steel.shownTicks,steel.shownStatus==0,false);
        else for(int[] pos:SteelContainer.POS)SteelGuiArt.slot(x+pos[0],y+pos[1]);
        GL11.glColor4f(1,1,1,1);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
        txt("Bulk Steel Factory",48,6,0x404040);txt(SteelContainer.statuses[steel.shownStatus],12,78,0x404040);
        txt(String.format(Locale.US,"%.2fs / %,d HE/t",steel.shownTicks/20.0,steel.shownHE),12,90,0x404040);txt("Inventory",21,104,0x404040);
    }
    @Override protected void renderToolTip(net.minecraft.item.ItemStack stack,int x,int y){
        int code=SteelRegistry.upgrade(stack);
        if(code==0){super.renderToolTip(stack,x,y);return;}
        List<String> lines=new ArrayList<String>(stack.getTooltip(mc.thePlayer,mc.gameSettings.advancedItemTooltips));
        lines.add("\u00a78Bulk Steel Factory effects (per level):");
        switch(code/4){
            case 1:lines.add("25% less base time; +500 HE/t");break;
            case 2:lines.add("+10 base ticks; -150 HE/t");break;
            case 3:lines.add("-1 coal dust; +125 HE/t");break;
            case 4:lines.add("Time / (1 + level)");lines.add("HE/t x (1 + 2 x level)");break;
            default:break;
        }
        lines.add("2 items maximum; each type capped at III");
        lines.add("Changes apply to the next batch");
        drawHoveringText(lines,x,y,fontRendererObj);
    }
    private void txt(String s,int x,int y,int color){fontRendererObj.drawString(s,x,y,color);}
    private boolean over(int mx,int my,int x,int y,int w,int h){return mx>=guiLeft+x&&mx<guiLeft+x+w&&my>=guiTop+y&&my<guiTop+y+h;}
    @Override public void drawScreen(int x,int y,float p){GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);try{GL11.glColor4f(1,1,1,1);super.drawScreen(x,y,p);
        if(over(x,y,173,17,18,54))drawHoveringText(Arrays.asList(String.format(Locale.US,"%,d / %,d HE",steel.shownPower,TileSteelFactory.MAX_HE),"HBM power cables on any side","No liquid or lava required"),x,y,fontRendererObj);
        else if(over(x,y,173,75,18,38)&&mc.thePlayer.inventory.getItemStack()==null){net.minecraft.inventory.Slot s=getSlotAtPositionSafe(x,y);if(s!=null&&!s.getHasStack())drawHoveringText(Arrays.asList("HBM upgrades: 2 maximum","One upgrade item per slot","Speed / Power Saving / Effectiveness / Overdrive","Combined levels of each type are capped at III","Current batch keeps its original upgrade effects"),x,y,fontRendererObj);}
        else if(over(x,y,23,16,100,56))drawHoveringText(Arrays.asList("16 iron + "+steel.shownCoal+" coal dust -> 16 HBM steel",String.format(Locale.US,"%.2f seconds at %,d HE/t",steel.shownTicks/20.0,steel.shownHE),String.format(Locale.US,"%,d HE / batch",steel.shownTicks*steel.shownHE)),x,y,fontRendererObj);
    }finally{GL11.glPopClientAttrib();GL11.glPopAttrib();}}
    private net.minecraft.inventory.Slot getSlotAtPositionSafe(int mx,int my){for(Object o:inventorySlots.inventorySlots){net.minecraft.inventory.Slot s=(net.minecraft.inventory.Slot)o;if(s.inventory==steel.factory&&s.slotNumber>=3&&over(mx,my,s.xDisplayPosition,s.yDisplayPosition,16,16))return s;}return null;}
}
