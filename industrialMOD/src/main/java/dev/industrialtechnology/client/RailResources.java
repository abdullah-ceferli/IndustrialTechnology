package dev.industrialtechnology.client;
import net.minecraft.client.resources.*;
public final class RailResources implements IResourceManagerReloadListener {public void onResourceManagerReload(IResourceManager m){RailMesh.clear();TrackRenderer.clear();}}
