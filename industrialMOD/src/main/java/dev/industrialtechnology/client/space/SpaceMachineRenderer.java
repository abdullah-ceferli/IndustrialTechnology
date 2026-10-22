package dev.industrialtechnology.client.space;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import dev.industrialtechnology.space.*;
import org.lwjgl.opengl.GL11;
public final class SpaceMachineRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation EQUIPMENT=new ResourceLocation("industrialtech:textures/models/space_equipment.png"),ROCKET=new ResourceLocation("industrialtech:textures/models/expedition_rocket.png");
    @Override public void renderTileEntityAt(TileEntity te,double x,double y,double z,float partial){TileSpaceMachine t=(TileSpaceMachine)te;if(t instanceof TileLaunchFacility||t.clientShuttle)return;
        if(t.kind!=SpaceRegistry.COMPUTER&&t.kind!=SpaceRegistry.TELESCOPE&&t.kind!=SpaceRegistry.ASSEMBLY&&!(t.kind==SpaceRegistry.LAUNCH&&t.clientRocket))return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+1,z+.5);int f=BlockSpaceMachine.front(t.getBlockMetadata());GL11.glRotatef(f==2?180:f==4?-90:f==5?90:0,0,1,0);GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);int light=t.getWorldObj().getLightBrightnessForSkyBlocks(t.xCoord,t.yCoord+1,t.zCoord,0);net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit,light&65535,light>>16);bindTexture(t.kind==SpaceRegistry.LAUNCH?ROCKET:EQUIPMENT);
            if(t.kind==SpaceRegistry.COMPUTER)SpaceModels.computer();else if(t.kind==SpaceRegistry.TELESCOPE)SpaceModels.telescope();else if(t.kind==SpaceRegistry.ASSEMBLY)SpaceModels.assembly();else SpaceModels.rocket();
        }finally{GL11.glPopMatrix();GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}}
}
