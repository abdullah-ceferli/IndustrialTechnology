package net.minecraft.block;
/** Isolated behavior fixture; never packaged into the mod. */
public class Block {
    public int render=-1, opacity, light;
    public boolean opaque, tile;
    public int getRenderType(){return render;}
    public boolean isOpaqueCube(){return opaque;}
    public int getLightOpacity(){return opacity;}
    public int getLightValue(){return light;}
    public boolean hasTileEntity(int metadata){return tile;}
}
