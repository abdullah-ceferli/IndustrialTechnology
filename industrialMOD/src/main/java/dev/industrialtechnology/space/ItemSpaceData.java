package dev.industrialtechnology.space;
import java.util.List;import net.minecraft.item.*;import net.minecraft.entity.player.EntityPlayer;import dev.industrialtechnology.core.ModRegistry;
public final class ItemSpaceData extends Item {
    public ItemSpaceData(){setMaxStackSize(1);setUnlocalizedName("industrialtech.research_hdd");setTextureName("industrialtech:research_hdd");setCreativeTab(ModRegistry.TAB);}
    @Override public void addInformation(ItemStack s,EntityPlayer p,List out,boolean advanced){out.add("Moon: "+ResearchData.progress(s,0)+" / "+ResearchData.required(0)+" ticks");out.add("Mars: "+ResearchData.progress(s,1)+" / "+ResearchData.required(1)+" ticks");out.add("Move this drive from computer to launch controller.");}
}
