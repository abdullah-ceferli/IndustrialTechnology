package dev.industrialtechnology.steel;
import net.minecraft.item.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
public final class ItemSteelFactory extends ItemBlock {
    public ItemSteelFactory(Block b){super(b);}
    @Override public void addInformation(ItemStack s,EntityPlayer p,List lines,boolean advanced){lines.add("16 iron + 4 coal dust -> 16 HBM steel");lines.add("5 seconds / batch; 500 HE/t while working");lines.add("HBM power cables; item pipes on any face");lines.add("2 HBM upgrade slots, one item each");lines.add("Speed / Power Saving / Effectiveness / Overdrive");if(!SteelRegistry.hasHBM)lines.add("Requires HBM Nuclear Tech Mod");}
}
