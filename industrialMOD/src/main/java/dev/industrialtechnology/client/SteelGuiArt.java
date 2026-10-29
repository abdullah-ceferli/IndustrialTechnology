package dev.industrialtechnology.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
/** Composes unmodified HBM atlas regions. Every animated overlay shares its base position. */
public final class SteelGuiArt extends Gui {
    private static final SteelGuiArt DRAW=new SteelGuiArt();
    private SteelGuiArt(){}
    public static void frame(int x,int y,int width,int height){drawRect(x+1,y,x+width-1,y+height,0xff000000);drawRect(x,y+1,x+width,y+height-1,0xff000000);drawRect(x+1,y+1,x+width-1,y+height-1,0xff555555);drawRect(x+1,y+1,x+width-2,y+height-2,0xffffffff);drawRect(x+4,y+4,x+width-4,y+height-4,0xffc6c6c6);}
    public static void slot(int x,int y){drawRect(x-1,y-1,x+17,y+17,0xff373737);drawRect(x,y,x+17,y+17,0xffeeeeee);drawRect(x,y,x+16,y+16,0xff8b8b8b);}
    // Occupied horizontal spans of the original HBM gray arrow, excluding its background.
    private static final int[] ARROW_BEGIN={0,15,15,15,15,15,15,1,1,1,15,15,15,15,15,15,0};
    private static final int[] ARROW_END={0,16,17,18,19,20,21,22,23,22,21,20,19,18,17,16,0};
    public static int arrowPixels(int progress,int ticks){return (int)(24L*Math.max(0,Math.min(Math.max(1,ticks),progress))/Math.max(1,ticks));}
    public static void process(int x,int y,int power,int progress,int ticks,boolean active,boolean nei){
        Minecraft mc=Minecraft.getMinecraft();GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(GuiSteelFactory.PANEL);
        int input=nei?32:44,energy=nei?142:174;
        DRAW.drawTexturedModalRect(x+input-1,y+17,79,17,20,54);
        DRAW.drawTexturedModalRect(x+input-20,y+37,63,37,14,14);
        DRAW.drawTexturedModalRect(x+input+22,y+35,101,35,24,17);
        DRAW.drawTexturedModalRect(x+input+50,y+31,129,31,26,26);
        DRAW.drawTexturedModalRect(x+energy-1,y+17,43,17,18,54);
        if(!nei){slot(x+174,y+76);slot(x+174,y+96);}else slot(x+114,y+36);
        GL11.glColor4f(1,1,1,1);
        // The hot flame sits exactly over the cold flame, without touching the inputs.
        if(active)DRAW.drawTexturedModalRect(x+input-20,y+37,176,0,14,14);
        int filled=arrowPixels(progress,ticks);
        for(int row=0;row<17;row++){int end=Math.min(ARROW_END[row],filled);if(end>ARROW_BEGIN[row])drawRect(x+input+22+ARROW_BEGIN[row],y+35+row,x+input+22+end,y+36+row,0xffffffff);}
        int fill=(int)(52L*Math.max(0,Math.min(1000000,power))/1000000);
        mc.getTextureManager().bindTexture(GuiSteelFactory.ELECTRIC);
        if(fill>0)DRAW.drawTexturedModalRect(x+energy,y+18+52-fill,176,12+52-fill,16,fill);
        GL11.glColor4f(1,1,1,1);
    }
}
