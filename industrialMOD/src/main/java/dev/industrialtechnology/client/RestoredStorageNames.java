package dev.industrialtechnology.client;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;

/** Apply only restored-item display names after ordinary language resources finish loading. */
public final class RestoredStorageNames implements IResourceManagerReloadListener {
    private static final String[] KEYS={
        "tile.machine_battery.name","tile.machine_lithium_battery.name",
        "tile.machine_schrabidium_battery.name","tile.machine_dineutronium_battery.name",
        "item.battery_spark_cell_6.name","item.battery_spark_cell_25.name",
        "item.battery_spark_cell_100.name","item.battery_spark_cell_1000.name"
    };
    @Override public void onResourceManagerReload(IResourceManager resources) {
        StringBuilder clean=new StringBuilder();
        for(String key:KEYS){
            String current=StatCollector.translateToLocal(key);
            String name=current.replaceAll("(?i)\\s*\\(LEGACY\\)","").trim();
            if(!name.equals(current))clean.append(key).append('=').append(name).append('\n');
        }
        if(clean.length()>0)StringTranslate.inject(new ByteArrayInputStream(clean.toString().getBytes(StandardCharsets.UTF_8)));
    }
}
