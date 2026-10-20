package dev.industrialtechnology.client;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import org.lwjgl.opengl.GL11;
/** Uses the classic Minecraft torch mesh, including wall tilt and ceiling mounting. */
public final class RedstoneRelayRenderer extends TileEntitySpecialRenderer {
    private final RenderBlocks renderer=new RenderBlocks();
    @Override public void renderTileEntityAt(TileEntity tile,double x,double y,double z,float partial){
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);GL11.glPushMatrix();
        try{
            bindTexture(TextureMap.locationBlocksTexture);GL11.glDisable(GL11.GL_LIGHTING);GL11.glEnable(GL11.GL_ALPHA_TEST);GL11.glAlphaFunc(GL11.GL_GREATER,.1f);GL11.glColor4f(1,1,1,1);
            Block block=tile.getBlockType();int face=tile.getBlockMetadata();GL11.glTranslated(x+.5,y+(face==0?1:0),z+.5);if(face==0)GL11.glRotatef(180,0,0,1);
            double dx=0,dz=0,tx=-.5,tz=-.5,ty=0;
            if(face>=2){ty=.2;if(face==5){tx-=.1;dx=-.4;}if(face==4){tx+=.1;dx=.4;}if(face==3){tz-=.1;dz=-.4;}if(face==2){tz+=.1;dz=.4;}}
            renderer.setOverrideBlockTexture(block.getIcon(tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord,0));
            Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(block.getMixedBrightnessForBlock(tile.getWorldObj(),tile.xCoord,tile.yCoord,tile.zCoord));t.setColorOpaque_F(1,1,1);
            renderer.renderTorchAtAngle(block,tx,ty,tz,dx,dz,0);t.draw();renderer.clearOverrideBlockTexture();
        }finally{GL11.glPopMatrix();GL11.glPopClientAttrib();GL11.glPopAttrib();}
    }
}
