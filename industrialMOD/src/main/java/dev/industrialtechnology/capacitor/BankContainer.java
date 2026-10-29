package dev.industrialtechnology.capacitor;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import dev.industrialtechnology.core.IndustrialTechnology;
public final class BankContainer extends Container {
    public final TileCapacitorBank bank;
    private int syncTicks,actions;private long actionTick=Long.MIN_VALUE;private NBTTagCompound previous;
    public BankContainer(TileCapacitorBank bank){this.bank=bank;}
    @Override public boolean canInteractWith(EntityPlayer p){return bank.usable(p);}
    public boolean allowAction(){long now=bank.getWorldObj().getTotalWorldTime();if(now!=actionTick){actionTick=now;actions=0;}return ++actions<=8;}
    @Override public void addCraftingToCrafters(ICrafting c){super.addCraftingToCrafters(c);if(c instanceof EntityPlayerMP)send((EntityPlayerMP)c);}
    public void send(EntityPlayerMP p){IndustrialTechnology.network.sendTo(new BankSnapshot(bank,windowId),p);}
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();if(bank.getWorldObj().isRemote||++syncTicks<BankConfig.guiInterval)return;syncTicks=0;NBTTagCompound s=bank.snapshot();if(s.equals(previous))return;previous=s;for(Object o:crafters)if(o instanceof EntityPlayerMP)IndustrialTechnology.network.sendTo(new BankSnapshot(bank,windowId,s),(EntityPlayerMP)o);}
    @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){return null;}
}
