package dev.industrialtechnology.quarry;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
/** Configuration stays on the physical quarry item, including while stored in inventories. */
public final class ItemBlockQuarry extends ItemBlock {
    public static final String SETTINGS = "IndustrialQuarrySettings";
    public ItemBlockQuarry(Block block) { super(block); }
    @Override @SideOnly(Side.CLIENT) public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        super.addInformation(stack, player, lines, advanced);
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(SETTINGS, 10)) return;
        NBTTagCompound n = stack.getTagCompound().getCompoundTag(SETTINGS); if (n.getInteger("Version") != 1) return;
        lines.add("Saved quarry settings");
        lines.add("VOID: " + (n.getBoolean("VoidEnabled") ? "ON" : "OFF") + " | " + (n.hasKey("Profile") ? n.getInteger("VoidCount") : n.getTagList("VoidEntries", 10).tagCount()) + " filters");
        lines.add("Harvest: " + (n.getBoolean("Silk") ? "Silk Touch" : "Fortune"));
        lines.add("Area: " + Integer.bitCount(n.getInteger("Selected") & ChunkArea.ALL) + " chunks");
    }
}
