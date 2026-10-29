package dev.industrialtechnology.capacitor;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
/** Redstone uses the normal weak/strong rules against a view that cannot fetch unloaded chunks. */
final class LoadedBankAccess implements IBlockAccess {
    private final World world;private final BankManager manager;
    LoadedBankAccess(World w,BankManager m){world=w;manager=m;}
    private boolean available(int x,int y,int z){return y>=0&&y<=255&&manager.loaded(x,z);}
    public Block getBlock(int x,int y,int z){return available(x,y,z)?world.getBlock(x,y,z):Blocks.air;}
    public TileEntity getTileEntity(int x,int y,int z){return available(x,y,z)?world.getTileEntity(x,y,z):null;}
    public int getBlockMetadata(int x,int y,int z){return available(x,y,z)?world.getBlockMetadata(x,y,z):0;}
    public int isBlockProvidingPowerTo(int x,int y,int z,int side){return getBlock(x,y,z).isProvidingStrongPower(this,x,y,z,side);}
    public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
    public boolean isSideSolid(int x,int y,int z,ForgeDirection side,boolean fallback){return available(x,y,z)?getBlock(x,y,z).isSideSolid(this,x,y,z,side):fallback;}
    public int getLightBrightnessForSkyBlocks(int x,int y,int z,int light){return light<<4;}
    public BiomeGenBase getBiomeGenForCoords(int x,int z){return BiomeGenBase.plains;}
    public int getHeight(){return 256;}
    public boolean extendedLevelsInChunkCache(){return false;}
    boolean powered(int x,int y,int z){for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS){int nx=x+d.offsetX,ny=y+d.offsetY,nz=z+d.offsetZ;Block b=getBlock(nx,ny,nz);
        if(b.shouldCheckWeakPower(this,nx,ny,nz,d.ordinal())){for(ForgeDirection strong:ForgeDirection.VALID_DIRECTIONS)if(isBlockProvidingPowerTo(nx+strong.offsetX,ny+strong.offsetY,nz+strong.offsetZ,strong.ordinal())>0)return true;}
        else if(b.isProvidingWeakPower(this,nx,ny,nz,d.ordinal())>0)return true;
    }return false;}
}
