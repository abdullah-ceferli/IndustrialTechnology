package net.minecraft.client.multiplayer;
import net.minecraft.world.World;
import net.minecraft.block.Block;
public class WorldClient extends World {
    public boolean func_147492_c(int x,int y,int z,Block block,int metadata){
        invalidations++;
        return super.setBlock(x,y,z,block,metadata,3);
    }
}
