package dev.industrialtechnology.test;
import java.io.*;import java.util.*;import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.*;import dev.industrialtechnology.quarry.*;
import net.minecraft.entity.item.EntityItem;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.nbt.*;
import net.minecraft.network.*;import net.minecraft.server.MinecraftServer;import net.minecraft.util.*;import net.minecraft.world.*;import net.minecraft.tileentity.*;
import net.minecraft.enchantment.Enchantment;import net.minecraftforge.common.*;import net.minecraftforge.common.util.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.world.BlockEvent;
/** Native survival/enchanted/creative harvest, physical pickup, item serialization, placement and actual restart. */
public final class QuarryItemIntegration {
    private static WorldServer world;private static Player player;private static int x,z,fixture;
    private static final class Player extends FakePlayer {
        Player(WorldServer w){super(w,new GameProfile(UUID.fromString("891a8412-5849-42ef-9b5b-0b3976c7ab82"),"QuarryItemOwner"));
            playerNetServerHandler=new NetHandlerPlayServer(MinecraftServer.getServer(),new NetworkManager(false),this){@Override public void sendPacket(Packet packet){}};
            theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);
        }
        @Override public void addChatMessage(IChatComponent ignored){}
    }
    public static final class Deny {
        private final TileQuarry q;public Deny(TileQuarry q){this.q=q;}
        @SubscribeEvent public void cancel(BlockEvent.BreakEvent e){if(e.x==q.xCoord&&e.y==q.yCoord&&e.z==q.zCoord)e.setCanceled(true);}
    }
    private static void check(boolean yes,String why){if(!yes)throw new AssertionError(why);}
    private static void setup(WorldServer w){world=w;player=new Player(w);x=(w.getSpawnPoint().posX&~15)+800;z=(w.getSpawnPoint().posZ&~15)+800;}
    private static TileQuarry quarry(int tier,boolean large){
        int bx=x+fixture++*16;world.getChunkFromChunkCoords(bx>>4,z>>4);world.setBlock(bx,80,z,ModRegistry.quarries[tier],2,3);
        TileQuarry q=(TileQuarry)world.getTileEntity(bx,80,z);q.setOwner(player);q.applySelection(ChunkArea.square3());q.silkMode=tier!=1;q.filter.enabled=q.filter.ignoreMetadata=true;
        Random random=new Random(1836281);
        for(int i=0;i<q.filter.getSizeInventory();i++){ItemStack icon=new ItemStack(Items.dye,32,i%16);NBTTagCompound n=new NBTTagCompound();n.setString("Marker","tier"+tier+"-slot"+i);
            if(large){byte[] bytes=new byte[800];random.nextBytes(bytes);n.setByteArray("PreservedData",bytes);}icon.setTagCompound(n);check(VoidFilter.valid(icon),"invalid test icon");q.filter.setInventorySlotContents(i,icon);}
        q.energy=1234;q.enabled=true;q.work=2;q.mined=999;q.cursor.set(12,(256-80)*256+4321);
        q.setInventorySlotContents(5,new ItemStack(Items.diamond,11));q.output.pending.add(new ItemStack(Items.coal,7));q.tickets.tick(true);check(q.loadedCount==9,"quarry fixture tickets missing");return q;
    }
    @SuppressWarnings("unchecked") private static List<EntityItem> drops(int bx,int by,int bz){List<EntityItem> list=world.getEntitiesWithinAABB(EntityItem.class,AxisAlignedBB.getBoundingBox(bx-2,by-2,bz-2,bx+3,by+3,bz+3));List<EntityItem> live=new ArrayList<EntityItem>();for(EntityItem e:list)if(!e.isDead)live.add(e);return live;}
    private static int count(List<EntityItem> drops,Item item){int n=0;for(EntityItem e:drops)if(e.getEntityItem().getItem()==item)n+=e.getEntityItem().stackSize;return n;}
    private static ItemStack harvest(TileQuarry q,Enchantment enchantment)throws Exception{
        player.theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);player.inventory.clearInventory(null,-1);player.inventory.currentItem=0;
        ItemStack tool=new ItemStack(Items.diamond_pickaxe);if(enchantment!=null)tool.addEnchantment(enchantment,enchantment==Enchantment.fortune?3:1);player.inventory.setInventorySlotContents(0,tool);player.setPosition(q.xCoord+.5,q.yCoord+1,q.zCoord+.5);
        Item blockItem=Item.getItemFromBlock(q.getBlockType());int bx=q.xCoord,by=q.yCoord,bz=q.zCoord;
        check(player.theItemInWorldManager.tryHarvestBlock(bx,by,bz),"native harvest failed");check(world.isAirBlock(bx,by,bz),"quarry not removed after harvesting");
        List<EntityItem> entities=drops(bx,by,bz);check(count(entities,blockItem)==1,"quarry drop duplicated or lost");
        check(count(entities,Items.diamond)==11&&count(entities,Items.coal)==7,"output items lost or duplicated");
        for(Object value:world.getPersistentChunks().values())if(value instanceof ForgeChunkManager.Ticket)check(!((ForgeChunkManager.Ticket)value).getModData().getString("MachineId").equals(q.machineId.toString()),"old ticket leaked");
        for(EntityItem e:entities){if(e.getEntityItem().getItem()==blockItem){e.delayBeforeCanPickup=0;e.onCollideWithPlayer(player);check(e.isDead,"actual pickup failed");}else e.setDead();}
        for(ItemStack stack:player.inventory.mainInventory)if(stack!=null&&stack.getItem()==blockItem){
            NBTTagCompound saved=new NBTTagCompound();stack.writeToNBT(saved);ItemStack restored=ItemStack.loadItemStackFromNBT(saved);
            PacketBuffer packet=new PacketBuffer(io.netty.buffer.Unpooled.buffer());packet.writeItemStackToBuffer(restored);ItemStack received=packet.readItemStackFromBuffer();check(!packet.isReadable()&&ItemStack.areItemStacksEqual(restored,received),"item NBT failed vanilla wire round trip");return restored;
        }throw new AssertionError("picked-up quarry missing");
    }
    private static TileQuarry place(ItemStack item,int bx,int by,int bz)throws Exception{
        world.getChunkFromChunkCoords(bx>>4,bz>>4);world.setBlock(bx,by-1,bz,Blocks.stone,0,3);player.inventory.clearInventory(null,-1);player.inventory.currentItem=0;player.inventory.setInventorySlotContents(0,item);player.setPosition(bx+.5,by+1,bz+.5);
        check(item.getItem().onItemUse(item,player,world,bx,by-1,bz,1,.5f,.5f,.5f)&&item.stackSize==0,"native item placement failed");
        TileEntity tile=world.getTileEntity(bx,by,bz);check(tile instanceof TileQuarry,"placed quarry missing");return(TileQuarry)tile;
    }
    private static void verify(TileQuarry q,int tier,boolean large){
        check(q.tier()==tier&&q.selected==ChunkArea.square3()&&q.silkMode==(tier!=1),"area/mode/tier lost");check(q.filter.enabled&&q.filter.ignoreMetadata&&q.filter.configured()==VoidFilter.capacity(tier),"VOID flags/capacity lost");
        for(int i=0;i<q.filter.getSizeInventory();i++){ItemStack icon=q.filter.getStackInSlot(i);check(icon!=null&&icon.stackSize==1&&icon.getItem()==Items.dye&&icon.getItemDamage()==i%16&&icon.getTagCompound().getString("Marker").equals("tier"+tier+"-slot"+i),"filter slot "+i+" lost its metadata/NBT");if(large)check(icon.getTagCompound().getByteArray("PreservedData").length==800,"large filter NBT lost");}
        check(!q.enabled&&!q.everStarted&&q.energy==0&&q.work==0&&q.mined==0&&q.skipped==0&&q.output.pending.isEmpty(),"world-specific runtime state copied");for(ItemStack stack:q.output.slots)check(stack==null,"output items duplicated into placed quarry");
        check(q.ownerProfile().getId().equals(player.getUniqueID()),"new placer not assigned owner");q.ensureMiningHeight();check(q.miningCeiling==q.yCoord&&q.cursor.y()<q.yCoord,"old mining height restored at new position");
    }
    private static void destroy(TileQuarry q){world.setBlockToAir(q.xCoord,q.yCoord,q.zCoord);}
    public static void run(WorldServer w,BiConsumer<String,Runnable> test){setup(w);
        for(int t=0;t<3;t++){final int tier=t;test.accept("MK"+(t+1)+" actual survival harvest pickup item NBT and placement retain every VOID entry and quarry setting",()->{try{TileQuarry original=quarry(tier,false);UUID old=original.machineId;ItemStack item=harvest(original,null);TileQuarry moved=place(item,original.xCoord+6,65,original.zCoord);verify(moved,tier,false);check(!old.equals(moved.machineId),"old machine identity copied");destroy(moved);}catch(Exception e){throw new RuntimeException(e);}});}
        test.accept("Fortune and Silk Touch tools each drop exactly one configured quarry with separate output items",()->{try{for(Enchantment enchantment:new Enchantment[]{Enchantment.fortune,Enchantment.silkTouch}){TileQuarry original=quarry(2,false);TileQuarry moved=place(harvest(original,enchantment),original.xCoord+6,65,original.zCoord);verify(moved,2,false);destroy(moved);}}catch(Exception e){throw new RuntimeException(e);}});
        test.accept("Ordinary crafted quarries retain clean default settings on placement",()->{try{TileQuarry q=place(new ItemStack(ModRegistry.quarries[0]),x+400,65,z);check(!q.enabled&&!q.silkMode&&!q.filter.enabled&&q.filter.configured()==0&&q.selected==ChunkArea.CENTER,"plain item inherited another quarry's settings");destroy(q);}catch(Exception e){throw new RuntimeException(e);}});
        test.accept("Drop queries return independent configuration copies without modifying live filter entries",()->{TileQuarry q=quarry(0,false);ItemStack one=q.getBlockType().getDrops(world,q.xCoord,q.yCoord,q.zCoord,2,0).get(0),two=q.getBlockType().getDrops(world,q.xCoord,q.yCoord,q.zCoord,2,0).get(0);one.getTagCompound().getCompoundTag(ItemBlockQuarry.SETTINGS).getTagList("VoidEntries",10).getCompoundTagAt(0).getCompoundTag("tag").setString("Marker","changed");check(q.filter.getStackInSlot(0).getTagCompound().getString("Marker").equals("tier0-slot0")&&!ItemStack.areItemStacksEqual(one,two),"configuration aliased live tile/other drop");destroy(q);});
        test.accept("Portable settings reject runtime state and sanitize chunk masks and out-of-range VOID positions",()->{try{ItemStack item=new ItemStack(ModRegistry.quarries[0]);NBTTagCompound root=new NBTTagCompound(),n=new NBTTagCompound();n.setInteger("Version",1);n.setInteger("Selected",Integer.MAX_VALUE);n.setBoolean("Enabled",true);n.setInteger("Energy",999999);n.setString("MachineId",UUID.randomUUID().toString());NBTTagList entries=new NBTTagList();NBTTagCompound entry=new NBTTagCompound();new ItemStack(Items.diamond).writeToNBT(entry);entry.setInteger("FilterSlot",49);entries.appendTag(entry);n.setTag("VoidEntries",entries);root.setTag(ItemBlockQuarry.SETTINGS,n);item.setTagCompound(root);TileQuarry q=place(item,x+416,65,z);check(!q.enabled&&q.energy==0&&q.filter.configured()==0&&ChunkArea.valid(q.selected,ModConfig.maxChunks),"item injected runtime state or invalid slots");destroy(q);}catch(Exception e){throw new RuntimeException(e);}});
        test.accept("Missing large-filter profiles fail closed without claiming successful restoration",()->{try{ItemStack item=new ItemStack(ModRegistry.quarries[2]);NBTTagCompound root=new NBTTagCompound(),n=new NBTTagCompound();n.setInteger("Version",1);n.setString("Profile",String.format("%064d",0));root.setTag(ItemBlockQuarry.SETTINGS,n);item.setTagCompound(root);TileQuarry q=place(item,x+432,65,z);check(!q.enabled&&q.filter.configured()==0&&q.issue.contains("unavailable"),"missing profile reported success");destroy(q);}catch(Exception e){throw new RuntimeException(e);}});
        test.accept("Canceled native quarry breaking preserves the machine settings and every stored item",()->{TileQuarry q=quarry(1,false);Deny deny=new Deny(q);MinecraftForge.EVENT_BUS.register(deny);try{player.inventory.currentItem=0;player.inventory.setInventorySlotContents(0,new ItemStack(Items.diamond_pickaxe));check(!player.theItemInWorldManager.tryHarvestBlock(q.xCoord,q.yCoord,q.zCoord)&&world.getTileEntity(q.xCoord,q.yCoord,q.zCoord)==q&&q.filter.configured()==36&&q.getStackInSlot(5).stackSize==11&&drops(q.xCoord,q.yCoord,q.zCoord).isEmpty(),"canceled break changed quarry or emitted drops");}finally{MinecraftForge.EVENT_BUS.unregister(deny);destroy(q);}});
        test.accept("Creative quarry destruction preserves vanilla no-block-drop behavior",()->{TileQuarry q=quarry(0,false);player.theItemInWorldManager.setGameType(WorldSettings.GameType.CREATIVE);player.inventory.currentItem=0;player.inventory.setInventorySlotContents(0,new ItemStack(Items.diamond_pickaxe));check(player.theItemInWorldManager.tryHarvestBlock(q.xCoord,q.yCoord,q.zCoord)&&world.isAirBlock(q.xCoord,q.yCoord,q.zCoord)&&count(drops(q.xCoord,q.yCoord,q.zCoord),Item.getItemFromBlock(ModRegistry.quarries[0]))==0,"creative quarry duplicated");for(EntityItem e:drops(q.xCoord,q.yCoord,q.zCoord))e.setDead();player.theItemInWorldManager.setGameType(WorldSettings.GameType.SURVIVAL);});
        test.accept("Full 50-entry NBT filters survive break placement and legacy item packets then remain stored for restart",()->{try{TileQuarry original=quarry(2,true);ItemStack item=harvest(original,null);check(item.getTagCompound().getCompoundTag(ItemBlockQuarry.SETTINGS).hasKey("Profile"),"large native filter fixture did not exercise overflow handling");TileQuarry moved=place(item,original.xCoord+6,65,original.zCoord);verify(moved,2,true);moved.setInventorySlotContents(5,new ItemStack(Items.diamond,11));moved.output.pending.add(new ItemStack(Items.coal,7));ItemStack carried=harvest(moved,null);player.inventory.clearInventory(null,-1);int cx=x+448;world.getChunkFromChunkCoords(cx>>4,z>>4);world.setBlock(cx,80,z,Blocks.chest,0,3);((TileEntityChest)world.getTileEntity(cx,80,z)).setInventorySlotContents(0,carried);NBTTagCompound n=new NBTTagCompound();n.setInteger("X",cx);n.setInteger("Z",z);CompressedStreamTools.writeCompressed(n,new FileOutputStream("quarry-item-fixture.nbt"));}catch(Exception e){throw new RuntimeException(e);}});
    }
    public static void restore(WorldServer w,BiConsumer<String,Runnable> test){setup(w);test.accept("Actual restart preserves the stored quarry item and all 50 large NBT filter selections on replacement",()->{try{NBTTagCompound n=CompressedStreamTools.readCompressed(new FileInputStream("quarry-item-fixture.nbt"));int bx=n.getInteger("X"),bz=n.getInteger("Z");w.getChunkFromChunkCoords(bx>>4,bz>>4);TileEntityChest chest=(TileEntityChest)w.getTileEntity(bx,80,bz);ItemStack item=chest.getStackInSlot(0);check(item!=null&&item.hasTagCompound(),"saved quarry item missing");chest.setInventorySlotContents(0,null);TileQuarry q=place(item,bx+4,65,bz);verify(q,2,true);destroy(q);}catch(Exception e){throw new RuntimeException(e);}});}
    private QuarryItemIntegration(){}
}
