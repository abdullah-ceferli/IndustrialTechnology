package com.hbm.test;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
public class Machine extends TileEntity {
    public int reads;
    public boolean unavailable;
    public AxisAlignedBB getRenderBoundingBox(){reads++;if(unavailable)throw new IllegalStateException("Bounds unavailable during rebuild");return new AxisAlignedBB();}
}
