package dev.industrialtechnology.client;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.opengl.GL11;
/** Restore GUI light positions before item browsers render in the same foreground pass. */
public abstract class GuiTooltips extends GuiContainer {
    protected GuiTooltips(Container container){super(container);}
    @Override protected void drawHoveringText(List lines,int x,int y,FontRenderer font){
        float previousZ=zLevel,previousItemZ=itemRender.zLevel;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try{super.drawHoveringText(lines,x,y,font);}
        finally{zLevel=previousZ;itemRender.zLevel=previousItemZ;GL11.glPopClientAttrib();GL11.glPopAttrib();}
    }
}
