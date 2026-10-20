package dev.industrialtechnology.automation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
final class ItemStackIdentity {
    static String name(ItemStack stack){Object name=Item.itemRegistry.getNameForObject(stack.getItem());return name==null?"":name.toString();}
    private ItemStackIdentity(){}
}
