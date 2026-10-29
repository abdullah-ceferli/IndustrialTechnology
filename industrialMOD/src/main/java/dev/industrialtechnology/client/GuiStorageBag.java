package dev.industrialtechnology.client;
import dev.industrialtechnology.bag.BagContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Original ProjectE 1.7.10 bag/chest atlas and slot positions, used unchanged. */
public final class GuiStorageBag extends GuiTooltips {
    private static final ResourceLocation TEXTURE=new ResourceLocation("industrialtech","textures/gui/storage_bag.png");
    public final BagContainer bag;
    public GuiStorageBag(BagContainer c){super(c);bag=c;xSize=255;ySize=230;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(TEXTURE);drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);}
    @Override public void drawScreen(int x,int y,float partial){GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);try{GL11.glColor4f(1,1,1,1);super.drawScreen(x,y,partial);}finally{GL11.glPopClientAttrib();GL11.glPopAttrib();}}
}
