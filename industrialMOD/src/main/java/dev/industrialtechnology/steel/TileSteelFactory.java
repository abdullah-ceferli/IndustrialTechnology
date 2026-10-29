package dev.industrialtechnology.steel;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.init.Items;
import net.minecraftforge.oredict.OreDictionary;

/** Fixed batch recipe. Materials remain in slots until the atomic completion. */
public class TileSteelFactory extends TileEntity implements ISidedInventory {
    public static final int BATCH=16,COAL=4,TICKS=100,HE_PER_TICK=500,MAX_HE=1000000;
    private static final int[] ACCESS={0,2,4};
    private final ItemStack[] slots=new ItemStack[10];
    protected long power;
    public int progress;
    public long batches;
    private int cachedA=-1,cachedB=-1;
    private SteelUpgradeProfile configured=SteelUpgradeProfile.BASE,locked=SteelUpgradeProfile.BASE;
    public SteelUpgradeProfile configuredProfile(){int a=SteelRegistry.upgrade(slots[8]),b=SteelRegistry.upgrade(slots[9]);if(a!=cachedA||b!=cachedB){configured=SteelUpgradeProfile.calculate(a,b);cachedA=a;cachedB=b;}return configured;}
    public SteelUpgradeProfile recipeProfile(){return progress>0?locked:configuredProfile();}
    public long getPower(){return power;}
    public void setPower(long value){long n=Math.max(0,Math.min(MAX_HE,value));if(n!=power){power=n;markDirty();}}
    public long getMaxPower(){return MAX_HE;}
    protected void connect(){}
    public static boolean ore(ItemStack s,String key){if(s==null)return false;for(int id:OreDictionary.getOreIDs(s))if(key.equals(OreDictionary.getOreName(id)))return true;return false;}
    public static boolean iron(ItemStack s){return s!=null&&(s.getItem()==Items.iron_ingot||ore(s,"ingotIron"));}
    public static boolean coal(ItemStack s){return s!=null&&(SteelRegistry.coal(s)||ore(s,"dustCoal"));}
    private int count(int from,int to){int n=0;for(int i=from;i<to;i++)if(slots[i]!=null&&(i<2?iron(slots[i]):coal(slots[i])))n+=slots[i].stackSize;return n;}
    private int space(ItemStack out){ItemStack s=slots[4];return s==null?Math.min(64,out.getMaxStackSize()):s.isItemEqual(out)&&ItemStack.areItemStackTagsEqual(s,out)?Math.max(0,Math.min(64,s.getMaxStackSize())-s.stackSize):0;}
    // Only old saves can populate retired slots; drain them into the three visible slots.
    private void drainLegacy(int from,int to){ItemStack source=slots[from],target=slots[to];if(source==null)return;if(target!=null&&(!source.isItemEqual(target)||!ItemStack.areItemStackTagsEqual(source,target)))return;int move=Math.min(source.stackSize,Math.min(64,source.getMaxStackSize())-(target==null?0:target.stackSize));if(move<=0)return;if(target==null){slots[to]=source.copy();slots[to].stackSize=move;}else target.stackSize+=move;source.stackSize-=move;if(source.stackSize<=0)slots[from]=null;markDirty();}
    public String status(){if(!SteelRegistry.hasHBM||SteelRegistry.steel(1)==null)return "HBM required";if(count(0,2)<BATCH)return "Need 16 iron ingots";if(count(2,4)<recipeProfile().coal)return "Need coal dust";if(space(SteelRegistry.steel(1))<BATCH)return "Steel output full";if(power<recipeProfile().hePerTick)return "Need HBM HE power";return "Producing steel";}
    private void consume(int from,int to,int n){for(int i=from;i<to&&n>0;i++){ItemStack s=slots[i];if(s==null||!(i<2?iron(s):coal(s)))continue;int take=Math.min(n,s.stackSize);s.stackSize-=take;n-=take;if(s.stackSize==0)slots[i]=null;}}
    private void output(){if(slots[4]==null)slots[4]=SteelRegistry.steel(BATCH);else slots[4].stackSize+=BATCH;}
    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote)return;
        drainLegacy(1,0);drainLegacy(3,2);for(int i=5;i<8;i++)drainLegacy(i,4);
        if(worldObj.getTotalWorldTime()%20==0)connect();
        boolean active="Producing steel".equals(status());
        if(active){if(progress==0)locked=configuredProfile();power-=locked.hePerTick;if(++progress>=locked.ticks){consume(0,2,BATCH);consume(2,4,locked.coal);output();progress=0;batches++;}markDirty();}
        // Missing materials reset work; lack of power/output capacity pauses paid work.
        else if((count(0,2)<BATCH||count(2,4)<recipeProfile().coal)&&progress!=0){progress=0;markDirty();}
        int meta=worldObj.getBlockMetadata(xCoord,yCoord,zCoord);
        if(((meta&8)!=0)!=active)worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,(meta&7)|(active?8:0),2);
    }
    @Override public int getSizeInventory(){return slots.length;}
    @Override public ItemStack getStackInSlot(int i){return i>=0&&i<slots.length?slots[i]:null;}
    @Override public ItemStack decrStackSize(int i,int n){ItemStack s=getStackInSlot(i);if(s==null||n<=0)return null;ItemStack result=s.splitStack(Math.min(n,s.stackSize));if(s.stackSize<=0)slots[i]=null;markDirty();return result;}
    @Override public ItemStack getStackInSlotOnClosing(int i){ItemStack s=getStackInSlot(i);if(i>=0&&i<slots.length)slots[i]=null;markDirty();return s;}
    @Override public void setInventorySlotContents(int i,ItemStack s){if(i<0||i>=slots.length)return;if(s!=null&&s.stackSize>Math.min(i>=8?1:64,s.getMaxStackSize()))throw new IllegalArgumentException("Oversized factory stack");slots[i]=s!=null&&s.stackSize>0?s:null;markDirty();}
    @Override public String getInventoryName(){return "container.industrialtech.bulk_steel_factory";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public boolean isUseableByPlayer(EntityPlayer p){return worldObj!=null&&worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&p.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;}
    @Override public void openInventory(){}
    @Override public void closeInventory(){}
    @Override public boolean isItemValidForSlot(int i,ItemStack s){return i>=8&&i<10?SteelRegistry.upgrade(s)!=0:i==0?iron(s):i==2&&coal(s);}
    @Override public int[] getAccessibleSlotsFromSide(int side){return ACCESS.clone();}
    @Override public boolean canInsertItem(int i,ItemStack s,int side){return i>=0&&i<8&&isItemValidForSlot(i,s);}
    @Override public boolean canExtractItem(int i,ItemStack s,int side){return i==4;}
    @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setLong("HE",power);n.setInteger("Progress",progress);n.setLong("Batches",batches);n.setInteger("BatchTicks",locked.ticks);n.setInteger("BatchHE",locked.hePerTick);n.setInteger("BatchCoal",locked.coal);NBTTagList list=new NBTTagList();for(int i=0;i<slots.length;i++)if(slots[i]!=null){NBTTagCompound s=new NBTTagCompound();s.setByte("Slot",(byte)i);slots[i].writeToNBT(s);list.appendTag(s);}n.setTag("Items",list);}
    @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);power=Math.max(0,Math.min(MAX_HE,n.getLong("HE")));locked=n.hasKey("BatchTicks")&&n.hasKey("BatchHE")&&n.hasKey("BatchCoal")?new SteelUpgradeProfile(n.getInteger("BatchTicks"),n.getInteger("BatchHE"),n.getInteger("BatchCoal")):SteelUpgradeProfile.BASE;progress=Math.max(0,Math.min(locked.ticks-1,n.getInteger("Progress")));cachedA=cachedB=-1;batches=Math.max(0,n.getLong("Batches"));java.util.Arrays.fill(slots,null);NBTTagList list=n.getTagList("Items",10);for(int j=0;j<list.tagCount();j++){NBTTagCompound s=list.getCompoundTagAt(j);int i=s.getByte("Slot")&255;ItemStack item=ItemStack.loadItemStackFromNBT(s);if(i<slots.length&&item!=null&&item.stackSize>0){item.stackSize=Math.min(item.stackSize,Math.min(i>=8?1:64,item.getMaxStackSize()));slots[i]=item;}}}
}
