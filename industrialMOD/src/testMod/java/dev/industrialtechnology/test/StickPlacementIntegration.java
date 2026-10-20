package dev.industrialtechnology.test;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import com.mojang.authlib.GameProfile;
import dev.industrialtechnology.core.ModRegistry;
import dev.industrialtechnology.wireless.*;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.*;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Exercises actual placement, interaction, permissions, drops and saved-world blocks. */
public final class StickPlacementIntegration {
    private static final UUID OWNER=UUID.fromString("93726336-204d-48f5-a153-d91963fdc7bd");
    private static final Item[] colors(){return new Item[]{ModRegistry.greenStick,ModRegistry.yellowStick,ModRegistry.blueStick};}
    private static final Block[] blocks(){return new Block[]{ModRegistry.monitoringStick,ModRegistry.redstoneRelay,ModRegistry.automationStick};}
    private static void check(boolean value,String text){if(!value)throw new AssertionError(text);}
    private static final class Player extends FakePlayer {
        boolean editable=true;
        Player(WorldServer w,UUID id,String name){super(w,new GameProfile(id,name));capabilities.allowEdit=true;capabilities.isCreativeMode=false;setSneaking(false);}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent text){}
        @Override public boolean canPlayerEdit(int x,int y,int z,int side,ItemStack stack){return editable&&super.canPlayerEdit(x,y,z,side,stack);}
    }
    public static final class DenyPlacement {
        final int x,y,z;
        DenyPlacement(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
        @SubscribeEvent public void cancel(BlockEvent.PlaceEvent e){if(e.x==x&&e.y==y&&e.z==z)e.setCanceled(true);}
    }
    private static TileRedstoneRelay torch(WorldServer w,int x,int y,int z){return(TileRedstoneRelay)w.getTileEntity(x,y,z);}
    private static void place(Player p,Item item,int x,int y,int z,int side){
        p.inventory.currentItem=1;p.inventory.setInventorySlotContents(1,new ItemStack(item,3));
        p.theItemInWorldManager.activateBlockOrUseItem(p,p.worldObj,p.getHeldItem(),x,y,z,side,.5f,.5f,.5f);
    }
    public static void run(WorldServer w,BiConsumer<String,Runnable> test){
        final int x=(w.getSpawnPoint().posX&~15)+32,z=(w.getSpawnPoint().posZ&~15)+32;
        w.getChunkFromChunkCoords(x>>4,z>>4);w.getChunkFromChunkCoords((x+15)>>4,(z+31)>>4);
        final Player p=new Player(w,OWNER,"TorchOwner");p.setPosition(x+3.5,81,z+2.5);
        final WirelessData data=WirelessData.get(w);
        final WirelessEndpoint[] floor=new WirelessEndpoint[3];
        test.accept("All three colors place on the ground without sneaking or a terminal and consume exactly one item",()->{
            for(int i=0;i<3;i++){w.setBlock(x+i*3,80,z,Blocks.stone,0,3);place(p,colors()[i],x+i*3,80,z,1);
                check(w.getBlock(x+i*3,81,z)==blocks()[i]&&p.getHeldItem().stackSize==2,"floor placement or item count failed for color "+i);
                check(torch(w,x+i*3,81,z).endpoint==null,"unlinked torch unexpectedly registered");}
        });
        test.accept("Unlinked torches remain placed and save their actual placer",()->{
            for(int i=0;i<3;i++){TileRedstoneRelay tile=torch(w,x+i*3,81,z);tile.updateEntity();NBTTagCompound n=new NBTTagCompound();tile.writeToNBT(n);TileRedstoneRelay copy=new TileRedstoneRelay();copy.readFromNBT(n);
                check(w.getBlock(x+i*3,81,z)==blocks()[i]&&copy.endpoint==null&&OWNER.equals(copy.placer),"unlinked torch vanished or lost owner");}
        });
        p.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));p.inventory.currentItem=0;
        final UUID network=WirelessAccess.terminal(p,p.getHeldItem(),true);
        test.accept("A terminal links previously placed Green Yellow and Blue torches without consuming another item",()->{
            for(int i=0;i<3;i++){ModRegistry.terminal.onItemUseFirst(p.getHeldItem(),p,w,x+i*3,81,z,1,.5f,.5f,.5f);floor[i]=data.devices.get(torch(w,x+i*3,81,z).endpoint);
                check(floor[i]!=null&&floor[i].physical&&floor[i].network.equals(network)&&WirelessAccess.relay(floor[i])==torch(w,x+i*3,81,z),"later linking failed");}
            check(p.getHeldItem().getItem()==ModRegistry.terminal&&p.getHeldItem().stackSize==1,"terminal consumed");
        });
        test.accept("Ground monitors safely report unsupported storage and Green Blue torches emit no redstone",()->{
            for(int i:new int[]{0,2}){check(MachineInspector.inspect(floor[i],0).status==WirelessAccess.Status.UNSUPPORTED,"ground invented inventory");
                check(!blocks()[i].canProvidePower()&&blocks()[i].isProvidingWeakPower(w,x+i*3,81,z,1)==0,"non-yellow torch emitted redstone");}
        });
        w.setBlock(x+3,80,z+4,WirelessTestFixtures.ports,0,3);
        final WirelessTestFixtures.Ports ports=(WirelessTestFixtures.Ports)w.getTileEntity(x+3,80,z+4);
        ports.setInventorySlotContents(0,new ItemStack(Items.iron_ingot,7));ports.setInventorySlotContents(1,new ItemStack(Items.gold_ingot,11));
        test.accept("Normal right-click mounts on input output faces before an interactive machine opens",()->{
            WirelessTestFixtures.portActivations=0;place(p,ModRegistry.greenStick,x+3,80,z+4,2);place(p,ModRegistry.blueStick,x+3,80,z+4,3);
            MachineInspector.Reading input=MachineInspector.inspect(data.devices.get(torch(w,x+3,80,z+3).endpoint),0),output=MachineInspector.inspect(data.devices.get(torch(w,x+3,80,z+5).endpoint),0);
            check(WirelessTestFixtures.portActivations==0&&input.inventory.slots.length==1&&input.inventory.amount(new ItemStack(Items.iron_ingot))==7&&output.inventory.amount(new ItemStack(Items.gold_ingot))==11,"input/output face placement or reading failed");
            check(input.inventory.insertable(0,new ItemStack(Items.iron_ingot))&&!output.inventory.insertable(0,new ItemStack(Items.gold_ingot)),"sided insertion permissions lost");
        });
        test.accept("Every colored torch mounts on all six floor wall ceiling faces and drops its correct item",()->{
            for(int color=0;color<3;color++)for(int face=0;face<6;face++){
                int tx=x+12,ty=84,tz=z+face*3;w.getChunkFromChunkCoords(tx>>4,tz>>4);p.setPosition(tx+2.5,ty+1,tz+.5);w.setBlock(tx,ty,tz,Blocks.stone,0,3);
                place(p,colors()[color],tx,ty,tz,face);ForgeDirection d=ForgeDirection.getOrientation(face);int rx=tx+d.offsetX,ry=ty+d.offsetY,rz=tz+d.offsetZ;
                check(w.getBlock(rx,ry,rz)==blocks()[color]&&w.getBlockMetadata(rx,ry,rz)==face,"mount failed "+color+"/"+face);
                check(w.getBlock(rx,ry,rz).getItemDropped(face,w.rand,0)==colors()[color],"wrong color drop");w.setBlockToAir(rx,ry,rz);
            }
        });
        p.setPosition(x+3.5,81,z+2.5);
        test.accept("Occupied faces preserve the existing torch and do not consume an item or duplicate links",()->{
            int count=data.devices.size();place(p,ModRegistry.yellowStick,x+3,80,z+4,2);
            check(w.getBlock(x+3,80,z+3)==ModRegistry.monitoringStick&&p.getHeldItem().stackSize==3&&data.devices.size()==count,"occupied face changed");
        });
        test.accept("Canceled Forge placement restores replaceable grass with no item charge or registration",()->{
            int tx=x+12,tz=z+24;w.getChunkFromChunkCoords(tx>>4,tz>>4);p.setPosition(tx+2.5,85,tz+.5);w.setBlock(tx,84,tz,Blocks.grass,0,3);w.setBlock(tx,85,tz,Blocks.tallgrass,1,3);
            DenyPlacement deny=new DenyPlacement(tx,85,tz);MinecraftForge.EVENT_BUS.register(deny);int count=data.devices.size();
            try{place(p,ModRegistry.yellowStick,tx,84,tz,1);}finally{MinecraftForge.EVENT_BUS.unregister(deny);}
            check(w.getBlock(tx,85,tz)==Blocks.tallgrass&&w.getBlockMetadata(tx,85,tz)==1&&p.getHeldItem().stackSize==3&&data.devices.size()==count,"canceled placement changed world/items: "+Block.blockRegistry.getNameForObject(w.getBlock(tx,85,tz))+" meta="+w.getBlockMetadata(tx,85,tz)+" stack="+p.getHeldItem().stackSize+" devices="+data.devices.size()+"/"+count);
        });
        final int unboundX=x+12,unboundZ=z+28;
        w.getChunkFromChunkCoords(unboundX>>4,unboundZ>>4);p.setPosition(unboundX+2.5,85,unboundZ+.5);w.setBlock(unboundX,84,unboundZ,Blocks.stone,0,3);
        ItemStack terminal=p.inventory.getStackInSlot(0);p.inventory.setInventorySlotContents(0,null);place(p,ModRegistry.blueStick,unboundX,84,unboundZ,1);p.inventory.setInventorySlotContents(0,terminal);
        test.accept("Another player cannot claim an unlinked torch placed by its owner",()->{
            Player other=new Player(w,UUID.fromString("18ca176f-c7b9-4f5f-bf32-999d7df23f3e"),"TorchOther");other.setPosition(unboundX+2.5,85,unboundZ+.5);other.inventory.setInventorySlotContents(0,new ItemStack(ModRegistry.terminal));other.inventory.currentItem=0;WirelessAccess.terminal(other,other.getHeldItem(),true);
            check(!ItemWirelessStick.linkPlaced(other,torch(w,unboundX,85,unboundZ))&&torch(w,unboundX,85,unboundZ).endpoint==null,"foreign owner claimed unbound torch");
        });
        p.setPosition(x+3.5,81,z+2.5);p.inventory.currentItem=0;
        test.accept("Unlinking a physical Green torch drops exactly one stick and preserves machine contents",()->{
            UUID id=torch(w,x+3,80,z+3).endpoint;TerminalContainer c=new TerminalContainer(p);c.action(WirelessCommand.SELECT,0,0,id.toString());int before=count(p,ModRegistry.greenStick);c.action(WirelessCommand.UNLINK,0,0,"");
            torch(w,x+3,80,z+3).updateEntity();int drops=0;for(Object value:w.getEntitiesWithinAABB(EntityItem.class,AxisAlignedBB.getBoundingBox(x+3,80,z+3,x+4,81,z+4))){ItemStack item=((EntityItem)value).getEntityItem();if(item.getItem()==ModRegistry.greenStick)drops+=item.stackSize;}
            check(w.isAirBlock(x+3,80,z+3)&&!data.devices.containsKey(id)&&count(p,ModRegistry.greenStick)==before&&drops==1,"physical unlink lost/duplicated stick");
            check(ports.getStackInSlot(0).stackSize==7&&ports.getStackInSlot(1).stackSize==11,"monitor changed machine inventory");
        });
        test.accept("Legacy virtual links still read real inventories and upgrade without losing identity or rules",()->{
            WirelessEndpoint old=new WirelessEndpoint();old.owner=OWNER;old.network=network;old.kind=WirelessEndpoint.Kind.AUTOMATION;old.dimension=0;old.x=x+3;old.y=80;old.z=z+4;old.side=2;old.block=String.valueOf(Block.blockRegistry.getNameForObject(WirelessTestFixtures.ports));old.tileToken=TargetAnchors.bind(ports);old.name="Saved input";old.rules[7].minimum=23;old.rules[7].target=floor[1].id;data.add(old);
            check(MachineInspector.inspect(old,0).inventory.amount(new ItemStack(Items.iron_ingot))==7,"legacy monitor stopped reading");place(p,ModRegistry.blueStick,x+3,80,z+4,2);
            check(old.id.equals(torch(w,x+3,80,z+3).endpoint)&&old.physical&&old.name.equals("Saved input")&&old.rules[7].minimum==23&&old.rules[7].target.equals(floor[1].id),"legacy upgrade lost state");
            NBTTagCompound n=old.write();n.removeTag("Physical");check(!WirelessEndpoint.read(n).hasPhysicalStick(),"legacy virtual requires missing torch");n.setInteger("Kind",WirelessEndpoint.Kind.CONTROL.ordinal());check(WirelessEndpoint.read(n).hasPhysicalStick(),"legacy Yellow lost relay requirement");
        });
        test.accept("Edit permissions block placement and device limits leave a usable unlinked torch",()->{
            int tx=x+12,tz=z+22;w.getChunkFromChunkCoords(tx>>4,tz>>4);p.setPosition(tx+2.5,85,tz+.5);w.setBlock(tx,84,tz,Blocks.stone,0,3);p.editable=false;
            try{place(p,ModRegistry.yellowStick,tx,84,tz,1);}finally{p.editable=true;}
            check(w.isAirBlock(tx,85,tz)&&p.getHeldItem().stackSize==3,"edit denial ignored");int limit=WirelessConfig.endpoints;WirelessConfig.endpoints=1;
            try{place(p,ModRegistry.yellowStick,tx,84,tz,1);check(w.getBlock(tx,85,tz)==ModRegistry.redstoneRelay&&torch(w,tx,85,tz).endpoint==null&&p.getHeldItem().stackSize==2,"limit discarded placement or bypassed registry");}finally{WirelessConfig.endpoints=limit;}
            check(ItemWirelessStick.linkPlaced(p,torch(w,tx,85,tz)),"torch could not link after capacity restored");
        });
        try{NBTTagCompound n=new NBTTagCompound();n.setInteger("X",x);n.setInteger("Z",z);n.setInteger("UnboundX",unboundX);n.setInteger("UnboundZ",unboundZ);CompressedStreamTools.writeCompressed(n,new FileOutputStream("stick-placement-fixtures.nbt"));}catch(IOException e){throw new RuntimeException(e);}
    }
    private static int count(Player p,Item item){int count=0;for(ItemStack stack:p.inventory.mainInventory)if(stack!=null&&stack.getItem()==item)count+=stack.stackSize;return count;}
    public static void restore(WorldServer w,BiConsumer<String,Runnable> test){
        test.accept("Actual restart preserves all three physical torch colors orientations owners and an unlinked torch",()->{
            try{NBTTagCompound n=CompressedStreamTools.readCompressed(new FileInputStream("stick-placement-fixtures.nbt"));int x=n.getInteger("X"),z=n.getInteger("Z");w.getChunkFromChunkCoords(x>>4,z>>4);
                for(int i=0;i<3;i++){TileRedstoneRelay tile=torch(w,x+i*3,81,z);WirelessEndpoint e=WirelessData.get(w).devices.get(tile.endpoint);check(w.getBlock(x+i*3,81,z)==blocks()[i]&&tile.getBlockMetadata()==1&&OWNER.equals(tile.placer)&&e!=null&&e.physical,"colored torch lost on restart");}
                int ux=n.getInteger("UnboundX"),uz=n.getInteger("UnboundZ");w.getChunkFromChunkCoords(ux>>4,uz>>4);TileRedstoneRelay unbound=torch(w,ux,85,uz);unbound.updateEntity();check(unbound.endpoint==null&&OWNER.equals(unbound.placer)&&w.getBlock(ux,85,uz)==ModRegistry.automationStick,"unbound torch did not survive restart");
            }catch(IOException e){throw new RuntimeException(e);}
        });
    }
}
