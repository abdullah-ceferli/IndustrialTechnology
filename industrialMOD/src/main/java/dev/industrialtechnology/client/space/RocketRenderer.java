package dev.industrialtechnology.client.space;
import net.minecraft.client.renderer.entity.Render;import net.minecraft.entity.Entity;import net.minecraft.util.ResourceLocation;import org.lwjgl.opengl.GL11;
public final class RocketRenderer extends Render {
    private static final ResourceLocation TEXTURE=new ResourceLocation("industrialtech:textures/models/expedition_rocket.png");
    public RocketRenderer(){shadowSize=1.5f;}
    @Override public void doRender(Entity e,double x,double y,double z,float yaw,float partial){GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();try{GL11.glTranslated(x,y,z);GL11.glRotatef(-yaw,0,1,0);GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);int light=e.getBrightnessForRender(partial);net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit,light&65535,light>>16);boolean shuttle=((dev.industrialtechnology.space.EntitySpaceRocket)e).shuttle();if(shuttle){GL11.glRotatef(2*yaw,0,1,0);GL11.glDisable(GL11.GL_CULL_FACE);bindTexture(new ResourceLocation("industrialtech:textures/models/shuttle_atlas.png"));ShuttleModel.draw();}else{bindTexture(TEXTURE);SpaceModels.rocket();}}finally{GL11.glPopMatrix();GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}}
    @Override protected ResourceLocation getEntityTexture(Entity e){return TEXTURE;}
}
