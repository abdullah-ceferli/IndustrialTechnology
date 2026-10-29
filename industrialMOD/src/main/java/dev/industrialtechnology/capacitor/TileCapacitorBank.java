package dev.industrialtechnology.capacitor;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import dev.industrialtechnology.core.ModRegistry;

public final class TileCapacitorBank extends TileEntity implements IEnergyHandler {
    public final BankLedger.Cell cell=new BankLedger.Cell(BankConfig.capacity,0,()->markDirty());
    final TileCapacitorBank[] adjacent=new TileCapacitorBank[6];
    final byte[] sides={2,2,2,2,2,2};
    boolean attached,claimed,powered;
    final TileEntity[] neighbors=new TileEntity[6];BankManager.Network network;
    long inputLimit=-1,outputLimit=-1,revision;int redstone;
    public int displayCharge;
    private int displayFaces;
    public boolean showsGauge(int face){return face>=0&&face<6&&(displayFaces&(1<<face))!=0;}
    public void toggleGauge(int face){if(worldObj==null||worldObj.isRemote||face<0||face>5)return;displayFaces^=1<<face;markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    @Override public boolean canUpdate(){return false;}
    @Override public void validate(){super.validate();if(worldObj!=null&&!worldObj.isRemote)BankManager.of(worldObj).attach(this);}
    @Override public void invalidate(){if(worldObj!=null&&!worldObj.isRemote)BankManager.of(worldObj).detach(this);super.invalidate();}
    @Override public void onChunkUnload(){if(worldObj!=null&&!worldObj.isRemote)BankManager.of(worldObj).detach(this);super.onChunkUnload();}
    @Override public void markDirty(){if(worldObj!=null&&!worldObj.isRemote&&worldObj.getChunkProvider().chunkExists(xCoord>>4,zCoord>>4))worldObj.getChunkFromChunkCoords(xCoord>>4,zCoord>>4).setChunkModified();}
    public BankManager.Network network(){return worldObj==null||worldObj.isRemote||claimed?null:BankManager.of(worldObj).ensure(this);}
    public SideMode mode(ForgeDirection d){return d==null||d==ForgeDirection.UNKNOWN?SideMode.DISABLED:SideMode.of(sides[d.ordinal()]);}
    public void setSide(int side,int mode){if(side<0||side>5||mode<0||mode>3)return;sides[side]=(byte)mode;markDirty();if(network!=null)network.portsDirty=true;worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord,zCoord,getBlockType());}
    @Override public boolean canConnectEnergy(ForgeDirection d){return mode(d)!=SideMode.DISABLED;}
    private BankManager.Network ready(ForgeDirection d,boolean input){if(isInvalid()||!(input?mode(d).input:mode(d).output))return null;BankManager.Network n=network();return n==null||n.busy||!input&&!n.outputEnabled()?null:n;}
    @Override public int receiveEnergy(ForgeDirection d,int max,boolean simulate){BankManager.Network n=ready(d,true);return n==null?0:n.ledger.receive(max,worldObj.getTotalWorldTime(),n.inputLimit,simulate);}
    @Override public int extractEnergy(ForgeDirection d,int max,boolean simulate){BankManager.Network n=ready(d,false);return n==null?0:n.ledger.extract(max,worldObj.getTotalWorldTime(),n.outputLimit,simulate);}
    public static int api(long n){return (int)Math.max(0,Math.min(Integer.MAX_VALUE,n));}
    @Override public int getEnergyStored(ForgeDirection d){BankManager.Network n=network();return n==null?0:api(n.ledger.stored());}
    @Override public int getMaxEnergyStored(ForgeDirection d){BankManager.Network n=network();return n==null?0:api(n.ledger.capacity());}
    void setDisplay(int level){if(displayCharge!=level){displayCharge=level;if(displayFaces!=0)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}}
    public boolean usable(EntityPlayer p){return !isInvalid()&&p.worldObj==worldObj&&p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64&&worldObj.getChunkProvider().chunkExists(xCoord>>4,zCoord>>4)&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this;}
    private void settings(NBTTagCompound tag){tag.setByte("DisplayFaces",(byte)displayFaces);tag.setByteArray("Sides",sides);tag.setLong("InputLimit",inputLimit);tag.setLong("OutputLimit",outputLimit);tag.setLong("Revision",revision);tag.setInteger("Redstone",redstone);}
    private void restoreSettings(NBTTagCompound tag){displayFaces=tag.getByte("DisplayFaces")&63;byte[] b=tag.getByteArray("Sides");if(b.length==6)for(int i=0;i<6;i++)sides[i]=(byte)(b[i]>=0&&b[i]<=3?b[i]:3);inputLimit=tag.hasKey("InputLimit")?Math.max(-1,tag.getLong("InputLimit")):-1;outputLimit=tag.hasKey("OutputLimit")?Math.max(-1,tag.getLong("OutputLimit")):-1;revision=Math.max(0,tag.getLong("Revision"));redstone=Math.max(0,Math.min(2,tag.getInteger("Redstone")));}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setLong("StoredRF",cell.energy());settings(tag);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);cell.load(tag.getLong("StoredRF"));restoreSettings(tag);}
    /** Preview is read-only. The harvest path claims and zeros only this physical bank once. */
    public ItemStack drop(boolean claim){
        ItemStack stack=new ItemStack(ModRegistry.capacitorBank);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("BankVersion",1);tag.setLong("StoredRF",claimed?0:cell.energy());settings(tag);stack.setTagCompound(tag);ItemBlockCapacitorBank.normalizeEmptyDefault(stack);
        if(claim&&!claimed){BankManager.of(worldObj).detach(this);cell.load(0);claimed=true;markDirty();}return stack;
    }
    public void restoreItem(ItemStack stack){
        if(stack==null||stack.stackSize<1||!stack.hasTagCompound()||stack.getTagCompound().getInteger("BankVersion")!=1)return;
        NBTTagCompound tag=stack.getTagCompound();if(stack.stackSize>1&&tag.getLong("StoredRF")>0)return;
        BankManager.of(worldObj).invalidate(network);cell.load(tag.getLong("StoredRF"));restoreSettings(tag);markDirty();
    }
    private NBTTagCompound visual(){NBTTagCompound t=new NBTTagCompound();t.setByte("DisplayFaces",(byte)displayFaces);t.setInteger("Charge",displayCharge);t.setByteArray("Sides",sides);return t;}
    @Override public Packet getDescriptionPacket(){return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,visual());}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){NBTTagCompound t=packet.func_148857_g();displayFaces=t.getByte("DisplayFaces")&63;displayCharge=Math.max(0,Math.min(16,t.getInteger("Charge")));byte[] b=t.getByteArray("Sides");if(b.length==6)System.arraycopy(b,0,sides,0,6);worldObj.markBlockRangeForRenderUpdate(xCoord,yCoord,zCoord,xCoord,yCoord,zCoord);}
    public NBTTagCompound snapshot(){
        NBTTagCompound t=new NBTTagCompound();BankManager.Network n=network();if(n==null)return t;long now=worldObj.getTotalWorldTime();
        t.setLong("Stored",n.ledger.stored());t.setLong("Capacity",n.ledger.capacity());t.setInteger("Banks",n.members.size());t.setLong("In",n.ledger.lastInput(now));t.setLong("Out",n.ledger.lastOutput(now));
        t.setLong("PhysicalIn",n.ledger.maxInput());t.setLong("PhysicalOut",n.ledger.maxOutput());t.setLong("LimitIn",n.maxInput());t.setLong("LimitOut",n.maxOutput());t.setBoolean("AutoIn",n.inputLimit<0);t.setBoolean("AutoOut",n.outputLimit<0);t.setInteger("Redstone",n.redstone);t.setBoolean("Active",n.outputEnabled());t.setByteArray("Sides",sides);t.setInteger("Facing",worldObj.getBlockMetadata(xCoord,yCoord,zCoord));return t;
    }
}
