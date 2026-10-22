package dev.industrialtechnology.quarry;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;

/** Normal settings travel inline. Large NBT filters use immutable world profiles to fit legacy item packets. */
public final class QuarryItemSettings extends WorldSavedData {
    private static final String KEY = "industrialtech_quarry_items";
    private final Map<String,NBTTagCompound> profiles = new LinkedHashMap<String,NBTTagCompound>();
    public QuarryItemSettings() { super(KEY); }
    public QuarryItemSettings(String key) { super(key); }
    private static QuarryItemSettings get() {
        WorldServer world = DimensionManager.getWorld(0);
        if (world == null) throw new IllegalStateException("Overworld unavailable");
        QuarryItemSettings data = (QuarryItemSettings) world.mapStorage.loadData(QuarryItemSettings.class, KEY);
        if (data == null) { data = new QuarryItemSettings(); world.mapStorage.setData(KEY, data); }
        return data;
    }
    public static NBTTagCompound pack(NBTTagCompound settings) {
        try {
            byte[] bytes = CompressedStreamTools.compress(settings);
            if (bytes.length <= 28000) return settings;
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder id = new StringBuilder(); for (byte b : digest) id.append(String.format(java.util.Locale.ROOT,"%02x",b & 255));
            QuarryItemSettings data = get(); String key = id.toString();
            if (!data.profiles.containsKey(key)) { data.profiles.put(key, (NBTTagCompound) settings.copy()); data.markDirty(); }
            NBTTagCompound summary = new NBTTagCompound(); summary.setInteger("Version",1); summary.setString("Profile",key);
            summary.setInteger("Selected",settings.getInteger("Selected")); summary.setBoolean("Silk",settings.getBoolean("Silk"));
            summary.setBoolean("VoidEnabled",settings.getBoolean("VoidEnabled")); summary.setBoolean("VoidIgnoreMetadata",settings.getBoolean("VoidIgnoreMetadata"));
            summary.setInteger("VoidCount",settings.getTagList("VoidEntries",10).tagCount()); return summary;
        } catch (IOException | NoSuchAlgorithmException impossible) { throw new IllegalStateException("Cannot store quarry settings", impossible); }
    }
    public static NBTTagCompound unpack(NBTTagCompound item) {
        if (!item.hasKey("Profile")) return (NBTTagCompound) item.copy();
        String id = item.getString("Profile"); if (!id.matches("[0-9a-f]{64}")) return null;
        NBTTagCompound settings = get().profiles.get(id); return settings == null ? null : (NBTTagCompound) settings.copy();
    }
    @Override public void writeToNBT(NBTTagCompound n) {
        NBTTagList list = new NBTTagList(); for (Map.Entry<String,NBTTagCompound> entry : profiles.entrySet()) {
            NBTTagCompound row = new NBTTagCompound(); row.setString("Id",entry.getKey()); row.setTag("Settings",entry.getValue().copy()); list.appendTag(row);
        } n.setTag("Profiles",list);
    }
    @Override public void readFromNBT(NBTTagCompound n) {
        profiles.clear(); NBTTagList list = n.getTagList("Profiles",10);
        for (int i=0;i<list.tagCount();i++) { NBTTagCompound row=list.getCompoundTagAt(i); String id=row.getString("Id");
            if (id.matches("[0-9a-f]{64}") && row.hasKey("Settings",10)) profiles.put(id,(NBTTagCompound) row.getCompoundTag("Settings").copy());
        }
    }
}
