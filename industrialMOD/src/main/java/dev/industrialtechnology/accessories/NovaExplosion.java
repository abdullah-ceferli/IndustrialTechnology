/** Adapted from ProjectE PE1.10.1 NovaExplosion; see META-INF/notices and ASSET_CREDITS.md. */
package dev.industrialtechnology.accessories;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class NovaExplosion
extends Explosion {
    private World worldObj;

    NovaExplosion(World world, Entity entity, double x, double y, double z, float radius) {
        super(world, entity, x, y, z, radius);
        this.isFlaming = true;
        this.isSmoking = true;
        this.worldObj = world;
    }

    public void doExplosionA() {
        float f = this.explosionSize;
        HashSet<ChunkPosition> hashset = Sets.newHashSet();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    if (i != 0 && i != 15 && j != 0 && j != 15 && k != 0 && k != 15) continue;
                    double d0 = (float)i / 15.0f * 2.0f - 1.0f;
                    double d1 = (float)j / 15.0f * 2.0f - 1.0f;
                    double d2 = (float)k / 15.0f * 2.0f - 1.0f;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    d0 /= d3;
                    d1 /= d3;
                    d2 /= d3;
                    double d5 = this.explosionX;
                    double d6 = this.explosionY;
                    double d7 = this.explosionZ;
                    float f2 = 0.3f;
                    for (float f1 = this.explosionSize * (0.7f + this.worldObj.rand.nextFloat() * 0.6f); f1 > 0.0f; f1 -= f2 * 0.75f) {
                        int l1;
                        int k1;
                        int j1 = MathHelper.floor_double((double)d5);
                        k1 = MathHelper.floor_double(d6); l1 = MathHelper.floor_double(d7);
                        if(!this.worldObj.blockExists(j1,k1,l1)) break;
                        Block block = this.worldObj.getBlock(j1,k1,l1);
                        if (block.getMaterial() != Material.air) {
                            float f3 = this.exploder != null ? this.exploder.func_145772_a((Explosion)this, this.worldObj, j1, k1, l1, block) : block.getExplosionResistance(this.exploder, this.worldObj, j1, k1, l1, this.explosionX, this.explosionY, this.explosionZ);
                            f1 -= (f3 + 0.3f) * f2;
                        }
                        if (f1 > 0.0f && (this.exploder == null || this.exploder.func_145774_a((Explosion)this, this.worldObj, j1, k1, l1, block, f1))) {
                            hashset.add(new ChunkPosition(j1, k1, l1));
                        }
                        d5 += d0 * (double)f2;
                        d6 += d1 * (double)f2;
                        d7 += d2 * (double)f2;
                    }
                }
            }
        }
        this.affectedBlockPositions.addAll(hashset);
        this.explosionSize = f;
        ForgeEventFactory.onExplosionDetonate((World)this.worldObj, (Explosion)this, Collections.emptyList(), (double)this.explosionSize);
    }

    public void doExplosionB(boolean spawnParticles) {
        this.worldObj.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0f, (1.0f + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2f) * 0.7f);
        if (this.explosionSize >= 2.0f && this.isSmoking) {
            this.worldObj.spawnParticle("hugeexplosion", this.explosionX, this.explosionY, this.explosionZ, 1.0, 0.0, 0.0);
        } else {
            this.worldObj.spawnParticle("largeexplode", this.explosionX, this.explosionY, this.explosionZ, 1.0, 0.0, 0.0);
        }
        ArrayList<net.minecraft.item.ItemStack> list = Lists.newArrayList();
        if (this.isSmoking) {
            for (Object affected : this.affectedBlockPositions) {
                ChunkPosition chunkposition=(ChunkPosition)affected;
                int i = chunkposition.chunkPosX;
                int j = chunkposition.chunkPosY;
                int k = chunkposition.chunkPosZ;
                if (!this.worldObj.blockExists(i,j,k) || this.exploder instanceof net.minecraft.entity.player.EntityPlayerMP && !AccessoryWorld.permitted((net.minecraft.entity.player.EntityPlayerMP)this.exploder,i,j,k)) continue;
                Block block = this.worldObj.getBlock(i, j, k);
                if (spawnParticles) {
                    double d0 = (float)i + this.worldObj.rand.nextFloat();
                    double d1 = (float)j + this.worldObj.rand.nextFloat();
                    double d2 = (float)k + this.worldObj.rand.nextFloat();
                    double d3 = d0 - this.explosionX;
                    double d4 = d1 - this.explosionY;
                    double d5 = d2 - this.explosionZ;
                    double d6 = MathHelper.sqrt_double((double)(d3 * d3 + d4 * d4 + d5 * d5));
                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5 / (d6 / (double)this.explosionSize + 0.1);
                    this.worldObj.spawnParticle("explode", (d0 + this.explosionX * 1.0) / 2.0, (d1 + this.explosionY * 1.0) / 2.0, (d2 + this.explosionZ * 1.0) / 2.0, d3 *= (d7 *= (double)(this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3f)), d4 *= d7, d5 *= d7);
                    this.worldObj.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);
                }
                if (block.getMaterial() == Material.air) continue;
                ArrayList drops = block.getDrops(this.worldObj, i, j, k, this.worldObj.getBlockMetadata(i, j, k), 0);
                if (drops != null && drops.size() > 0) {
                    list.addAll(drops);
                }
                block.onBlockExploded(this.worldObj, i, j, k, (Explosion)this);
            }
            EntityLivingBase ent = this.getExplosivePlacedBy();
            if (ent != null) {
                AccessoryWorld.loot(list, this.worldObj, ent.posX, ent.posY, ent.posZ);
            } else {
                AccessoryWorld.loot(list, this.worldObj, this.explosionX, this.explosionY, this.explosionZ);
            }
        }
    }
}

