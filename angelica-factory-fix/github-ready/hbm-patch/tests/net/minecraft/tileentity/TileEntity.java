package net.minecraft.tileentity;
import net.minecraft.util.AxisAlignedBB;
public class TileEntity {
    public boolean invalid;
    public boolean renderable=true;
    public AxisAlignedBB getRenderBoundingBox(){return new AxisAlignedBB();}
    public boolean func_145837_r(){return invalid;}
}
