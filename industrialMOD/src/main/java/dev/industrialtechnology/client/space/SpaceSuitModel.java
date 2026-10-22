package dev.industrialtechnology.client.space;
import net.minecraft.client.model.*;import net.minecraft.entity.EntityLivingBase;
/** Dedicated UV space for packs/helmet fittings; standard 1.7.10 armor slots. */
public final class SpaceSuitModel extends ModelBiped {
    private final ModelRenderer pack,valveLeft,valveRight,rim,shoulderLeft,shoulderRight,kneeLeft,kneeRight,soleLeft,soleRight;private final int tier;
    public SpaceSuitModel(int tier,int slot){super(slot==2?.4f:.65f,0,64,64);this.tier=tier;
        pack=new ModelRenderer(this,0,32);pack.addBox(-4,0,2,8,10,3+tier,.25f);bipedBody.addChild(pack);
        valveLeft=new ModelRenderer(this,40,32);valveLeft.addBox(-5,-5,-1,1,3,3,.1f);bipedHead.addChild(valveLeft);
        valveRight=new ModelRenderer(this,40,32);valveRight.addBox(4,-5,-1,1,3,3,.1f);bipedHead.addChild(valveRight);
        rim=new ModelRenderer(this,40,48);rim.addBox(-4,-7,-4.6f,8,1,1,.1f);bipedHead.addChild(rim);
        valveLeft.showModel=valveRight.showModel=tier>0;rim.showModel=tier==2;
        shoulderRight=new ModelRenderer(this,40,32);shoulderRight.addBox(-4,-2.5f,-3,6,3,6);bipedRightArm.addChild(shoulderRight);
        shoulderLeft=new ModelRenderer(this,40,32);shoulderLeft.addBox(-2,-2.5f,-3,6,3,6);bipedLeftArm.addChild(shoulderLeft);
        kneeRight=new ModelRenderer(this,40,32);kneeRight.addBox(-2,5.2f,-2.7f,4,3,1);bipedRightLeg.addChild(kneeRight);
        kneeLeft=new ModelRenderer(this,40,32);kneeLeft.addBox(-2,5.2f,-2.7f,4,3,1);bipedLeftLeg.addChild(kneeLeft);
        soleRight=new ModelRenderer(this,40,48);soleRight.addBox(-2,10,-2.8f,4,2,5,.1f);bipedRightLeg.addChild(soleRight);
        soleLeft=new ModelRenderer(this,40,48);soleLeft.addBox(-2,10,-2.8f,4,2,5,.1f);bipedLeftLeg.addChild(soleLeft);
    }
    public SpaceSuitModel configure(EntityLivingBase e,int slot){bipedHead.showModel=slot==0;bipedHeadwear.showModel=false;bipedBody.showModel=slot==1||slot==2;bipedRightArm.showModel=bipedLeftArm.showModel=slot==1;bipedRightLeg.showModel=bipedLeftLeg.showModel=slot==2||slot==3;pack.showModel=slot==1;shoulderLeft.showModel=shoulderRight.showModel=tier>0&&slot==1;kneeLeft.showModel=kneeRight.showModel=tier>0&&slot==2;soleLeft.showModel=soleRight.showModel=tier==2&&slot==3;isSneak=e.isSneaking();isRiding=e.ridingEntity!=null;heldItemRight=e.getHeldItem()==null?0:1;return this;}
}
