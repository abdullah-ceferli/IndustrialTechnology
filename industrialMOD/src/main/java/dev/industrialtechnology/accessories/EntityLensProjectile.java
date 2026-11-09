package dev.industrialtechnology.accessories;
import net.minecraft.entity.projectile.EntityThrowable;import net.minecraft.entity.player.*;import net.minecraft.world.*;import net.minecraft.util.*;import net.minecraft.nbt.NBTTagCompound;
public final class EntityLensProjectile extends EntityThrowable {
 private int charge;
 public EntityLensProjectile(World w){super(w);}public EntityLensProjectile(World w,EntityPlayer p,int c){super(w,p);charge=Math.max(0,Math.min(7,c));}
 protected float getGravityVelocity(){return 0;}
 protected void onImpact(MovingObjectPosition hit){if(!worldObj.isRemote&&getThrower() instanceof EntityPlayerMP)AccessoryWorld.nova(worldObj,getThrower(),posX,posY,posZ,ItemCatalyst.SHOT_RADIUS[charge]);setDead();}
 public void onUpdate(){super.onUpdate();if(worldObj.isRemote||isDead)return;if(ticksExisted>400||!worldObj.blockExists(MathHelper.floor_double(posX),MathHelper.floor_double(posY),MathHelper.floor_double(posZ))){setDead();return;}if(isInWater()){playSound("random.fizz",.7f,1.6f+(rand.nextFloat()-rand.nextFloat())*.4f);if(worldObj instanceof WorldServer)((WorldServer)worldObj).func_147487_a("largesmoke",posX,posY,posZ,1,0,0,0,0);setDead();}}
 public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setByte("Charge",(byte)charge);}public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);charge=Math.max(0,Math.min(7,n.getByte("Charge")));}
}
