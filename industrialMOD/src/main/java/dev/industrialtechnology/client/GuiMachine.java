package dev.industrialtechnology.client;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import dev.industrialtechnology.core.IndustrialTechnology;
import dev.industrialtechnology.machine.MachineContainer;
import dev.industrialtechnology.machine.MachineStatus;
import dev.industrialtechnology.network.ControlPacket;
import dev.industrialtechnology.quarry.ChunkArea;
import dev.industrialtechnology.quarry.MiningCursor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiMachine extends GuiContainer {
    public final MachineContainer container;
    protected NBTTagCompound state;
    protected int draft, gridX = 129, gridY = 49, cellSize = 12;
    protected boolean dirtySelection, waitingApply;
    protected final List<MetalButton> controls = new ArrayList<MetalButton>();
    public GuiMachine(MachineContainer container) {
        super(container); this.container = container; xSize = 316; ySize = 236;
        state = container.machine.snapshot(); draft = state.getInteger("Selected");
    }
    public void receive(NBTTagCompound update) {
        state = update;
        if (container.machine instanceof dev.industrialtechnology.quarry.TileQuarry)
        {
            dev.industrialtechnology.quarry.VoidFilter filter=((dev.industrialtechnology.quarry.TileQuarry)container.machine).filter;
            filter.enabled=update.getBoolean("VoidEnabled"); filter.ignoreMetadata=update.getBoolean("VoidIgnoreMetadata");
        }
        container.setFilterPage(state.getInteger("FilterPage"));
        if (waitingApply && state.getInteger("Selected") == draft) { waitingApply = dirtySelection = false; }
        if (!dirtySelection) draft = state.getInteger("Selected");
        container.setTab(state.getInteger("Tab")); updateButtons();
    }
    @Override public void updateScreen() { super.updateScreen(); updateButtons(); }
    @Override public void initGui() {
        super.initGui(); buttonList.clear(); controls.clear();
        configureButtons(); updateButtons();
    }
    protected void configureButtons() {
        button(10, 117, 7, 58, 15, "Control", "Machine controls and chunk selection");
        if (container.machine.isQuarry()) {
            button(11, 178, 7, 58, 15, "Output", "All 54 output slots; shift-click to extract");
            button(12, 239, 7, 68, 15, "Void Filter", "Configure tier-specific ghost icons; matching mined drops can be permanently deleted");
            button(5, 16, 49, 125, 14, "Auto Delete: OFF", "Matching mined drops will be permanently deleted instead of entering Quarry storage");
            button(6, 146, 49, 155, 14, "Exact Match", "Exact matches item, metadata, and NBT; Ignore Metadata still matches item and NBT");
            button(7, 16, 118, 58, 13, "Clear All", "Clear all filter entries; does not change real items");
            button(8, 250, 79, 51, 13, "Previous", "Previous filter page");
            button(9, 250, 98, 51, 13, "Next", "Next filter page");
            button(25, 12, 163, 58, 14, "Area: ON", "Show / Hide Mining Area locally; does not change mining or chunk loading");
        }
        if (container.machine.isQuarry()) {
            button(0, 16, 125, 80, 13, "Start", "Start/resume; Stop preserves progress and releases tickets");
            button(2, 221, 49, 80, 13, "Fortune", "Tier Fortune; switches off Silk Touch");
            button(3, 221, 64, 80, 13, "Silk Touch", "Supported block forms; switches off Fortune");
        }
        button(20, 112, 112, 21, 12, "1", "Select only the machine chunk");
        button(21, 136, 112, 21, 12, "3x3", "Select the central 3 x 3 chunks");
        button(22, 160, 112, 21, 12, "All", "Select all permitted chunks (up to 25)");
        button(23, 184, 112, 21, 12, "R", "Reset selection to the machine chunk");
        button(1, 112, 127, 48, 13, "Apply", "Apply the draft selection to the server");
        button(24, 163, 127, 44, 13, "View", "Show selected chunk outlines for 30 seconds");
    }
    protected void button(int id, int x, int y, int w, int h, String label, String tooltip) {
        MetalButton b = new MetalButton(id, guiLeft + x, guiTop + y, w, h, label, tooltip);
        buttonList.add(b); controls.add(b);
    }
    protected void updateButtons() {
        for (MetalButton b : controls) {
            b.visible = b.id >= 10 && b.id <= 12 || (b.id >= 5 && b.id <= 9 ? container.tab == 2 : b.id == 25 || container.tab == 0);
            b.enabled = true; b.selected = false;
            if (b.id == 0) { b.displayString = state.getBoolean("Enabled") ? "Stop" : "Start"; b.enabled = state.getInteger("Status") != MachineStatus.COMPLETED.ordinal(); }
            if (b.id == 1) { b.selected = dirtySelection; b.enabled = dirtySelection && !waitingApply; }
            if (b.id == 2) { b.displayString = "Fortune " + new String[]{"I", "II", "III"}[Math.max(0, Math.min(2, state.getInteger("Tier")))]; b.selected = !state.getBoolean("Silk"); b.enabled = state.getBoolean("FortuneAllowed"); }
            if (b.id == 3) { b.selected = state.getBoolean("Silk"); b.enabled = state.getBoolean("SilkAllowed"); }
            if (b.id == 10) b.selected = container.tab == 0;
            if (b.id == 11) b.selected = container.tab == 1;
            if (b.id == 12) b.selected = container.tab == 2;
            if (b.id == 5) { b.displayString = "Auto Delete: " + (state.getBoolean("VoidEnabled") ? "ON" : "OFF"); b.selected = state.getBoolean("VoidEnabled"); }
            if (b.id == 6) b.displayString = state.getBoolean("VoidIgnoreMetadata") ? "Ignore Metadata" : "Exact Match";
            if (b.id == 8) b.enabled=container.filterPage>0;
            if (b.id == 9) b.enabled=container.filterPage+1<container.filterPages();
            if (b.id == 25) b.displayString = "Area: " + (OverlaySettings.quarries ? "ON" : "OFF");
            if (b.id == 24) { b.enabled = state.getBoolean("PreviewAllowed"); b.selected = AreaPreview.isActive(container.machine); }
        }
    }
    private void send(int action, int value) { IndustrialTechnology.network.sendToServer(new ControlPacket(container.windowId, action, value)); }
    @Override protected void actionPerformed(GuiButton b) {
        if (!b.enabled) return;
        switch (b.id) {
            case 0: send(ControlPacket.TOGGLE, 0); break;
            case 1: waitingApply = true; send(ControlPacket.APPLY, draft); break;
            case 2: state.setBoolean("Silk", false); send(ControlPacket.FORTUNE, 0); break;
            case 3: state.setBoolean("Silk", true); send(ControlPacket.SILK, 0); break;
            case 10: container.setTab(0); send(ControlPacket.TAB, 0); break;
            case 11: container.setTab(1); send(ControlPacket.TAB, 1); break;
            case 12: container.setTab(2); send(ControlPacket.TAB, 2); break;
            case 5: send(ControlPacket.VOID_TOGGLE, 0); break;
            case 6: send(ControlPacket.VOID_MODE, 0); break;
            case 7: send(ControlPacket.VOID_CLEAR, 0); break;
            case 8: case 9:
                container.setFilterPage(container.filterPage+(b.id==9?1:-1));
                send(ControlPacket.FILTER_PAGE,container.filterPage);break;
            case 25: OverlaySettings.toggle(container.machine.isQuarry()); break;
            case 20: case 23: setDraft(ChunkArea.CENTER); break;
            case 21: setDraft(ChunkArea.square3()); break;
            case 22: setDraft(ChunkArea.ALL); break;
            case 24: AreaPreview.toggle(container.machine, state.getInteger("Selected")); break;
            default: break;
        }
        updateButtons();
    }
    protected void setDraft(int mask) {
        draft = ChunkArea.sanitize(mask, state.getInteger("MaxChunks"));
        dirtySelection = draft != state.getInteger("Selected"); waitingApply = false;
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        int x = mouseX - guiLeft, y = mouseY - guiTop;
        if (container.tab == 0 && button == 0 && x >= gridX && x < gridX + cellSize * 5 && y >= gridY && y < gridY + cellSize * 5) {
            int cell = ((y - gridY) / cellSize) * 5 + (x - gridX) / cellSize;
            int candidate = draft ^ (1 << cell);
            if (cell != 12 && ChunkArea.valid(candidate, state.getInteger("MaxChunks"))) setDraft(candidate);
            updateButtons(); return;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public void drawScreen(int x, int y, float partial) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_CLIENT_VERTEX_ARRAY_BIT);
        try { GL11.glColor4f(1, 1, 1, 1); super.drawScreen(x, y, partial); }
        finally { GL11.glPopClientAttrib(); GL11.glPopAttrib(); }
    }
    protected void drawPanel(String texture, int textureWidth) {
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(new ResourceLocation(IndustrialTechnology.MODID, "textures/gui/" + texture + ".png"));
        Tessellator t = Tessellator.instance; t.startDrawingQuads();
        t.addVertexWithUV(guiLeft, guiTop + ySize, 0, 0, ySize / 256.0);
        t.addVertexWithUV(guiLeft + xSize, guiTop + ySize, 0, xSize / (double) textureWidth, ySize / 256.0);
        t.addVertexWithUV(guiLeft + xSize, guiTop, 0, xSize / (double) textureWidth, 0);
        t.addVertexWithUV(guiLeft, guiTop, 0, 0, 0); t.draw();
    }
    protected void drawGrid() {
        int completed = state.getInteger("Completed"), active = state.getInteger("Cell"), applied = state.getInteger("Selected");
        for (int cell = 0; cell < 25; cell++) {
            int px = guiLeft + gridX + cell % 5 * cellSize, py = guiTop + gridY + cell / 5 * cellSize;
            int color = (draft & (1 << cell)) == 0 ? 0xff282d31 : (completed & (1 << cell)) != 0 ? 0xff526e59
                : container.machine.isQuarry() && cell == active && state.getInteger("Status") == MachineStatus.MINING.ordinal() ? 0xffb08749 : 0xff5c727d;
            drawRect(px, py, px + cellSize - 1, py + cellSize - 1, 0xff141a1e);
            drawRect(px + 1, py + 1, px + cellSize - 2, py + cellSize - 2, color);
            if ((draft & (1 << cell)) != (applied & (1 << cell))) drawRect(px + 1, py + 1, px + cellSize - 2, py + 2, 0xffd5b474);
            if (cell == 12) { int c = cellSize / 2; drawRect(px + c - 1, py + c - 2, px + c + 2, py + c + 3, 0xffe3d2aa); drawRect(px + c - 2, py + c - 1, px + c + 3, py + c + 2, 0xffe3d2aa); }
        }
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawPanel(container.tab == 1 ? "storage" : container.tab == 2 ? "void_filter" : "controls", 512);
        if(container.tab==2){
            drawRect(guiLeft+77,guiTop+72,guiLeft+241,guiTop+128,0xff272e33);
            int size=((dev.industrialtechnology.quarry.TileQuarry)container.machine).filter.getSizeInventory();
            for(int i=0;i<Math.min(25,size-container.filterPage*25);i++){
                int px=guiLeft+78+i%9*18,py=guiTop+73+i/9*18;
                drawRect(px-1,py-1,px+17,py+17,0xff171e22);
                drawRect(px,py,px+16,py+16,0xff414c52);
                drawRect(px+1,py+1,px+16,py+16,0xff303a40);
            }
        }
        if (container.tab == 0) {
            int cap = Math.max(1, state.getInteger("Capacity"));
            int bar = (int) (43L * state.getInteger("Energy") / cap);
            if (state.getInteger("Energy") > 0) bar = Math.max(1, bar);
            drawRect(guiLeft + 17, guiTop + 72, guiLeft + 27, guiTop + 117, 0xff121719);
            drawRect(guiLeft + 18, guiTop + 116 - bar, guiLeft + 26, guiTop + 116, 0xffc49346);
            drawGrid();
        }
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        text(container.machine.machineName(), 10, 10, 0xe3d7ba);
        text("Inventory", 78, 141, 0xa1aaa9);
        if (container.tab == 1) {
            text("OUTPUT", 11, 33, 0xe0b975); text("54 slots", 11, 45, 0xa1aaa9);
            text("Extract", 11, 63, 0xb4bdbc); text("with pipes", 11, 75, 0xb4bdbc);
            text("or shift", 11, 92, 0x8f999b); text("+ click", 11, 104, 0x8f999b);
            text("Pending: " + state.getInteger("Pending"), 11, 116, 0xa1aaa9);
        } else if (container.tab == 2) {
            text("VOID FILTER", 16, 36, 0xe0b975);
            text("Ghost", 16, 76, 0xa1aaa9); text("icons", 16, 88, 0xa1aaa9);
            text("Left: set icon; Right: clear", 78, 64, 0xa1aaa9);
            text("Page "+(container.filterPage+1)+"/"+container.filterPages(),250,66,0xb4bdbc);
            text("Configured: " + state.getInteger("VoidConfigured") + " / " + state.getInteger("VoidCapacity"),111,132,0xb4bdbc);
        } else {
            int ordinal = Math.max(0, Math.min(MachineStatus.values().length - 1, state.getInteger("Status")));
            String label = MachineStatus.values()[ordinal].label();
            text("STATUS", 16, 36, 0x929c9c);
            List<String> lines = fontRendererObj.listFormattedStringToWidth(label, 82);
            for (int i = 0; i < Math.min(2, lines.size()); i++) text(lines.get(i), 16, 48 + i * 10, ordinal == MachineStatus.CHUNK_LOADING_ERROR.ordinal() ? 0xd9a270 : 0xd4c9ad);
            text(compact(state.getInteger("Energy")) + " RF", 32, 73, 0xd6b77b);
            text("/ " + compact(state.getInteger("Capacity")), 32, 85, 0x8f999b);
            text(state.getInteger("Input") + " RF/t", 32, 98, 0xb4bdbc);
            if (container.machine.isQuarry()) {
                text(state.getInteger("Cost") + "/op", 32, 110, 0xb4bdbc);
                text("HARVEST", 221, 36, 0x929c9c);
                text(state.getInteger("MeasuredSpeed") + " blk/s", 221, 82, 0xb4bdbc);
                int cell = Math.max(0, Math.min(24, state.getInteger("Cell")));
                text("Chunk " + ChunkArea.dx(cell) + "," + ChunkArea.dz(cell), 221, 95, 0xb4bdbc);
                text("Y: " + Math.max(0, state.getInteger("Y")), 221, 108, 0xb4bdbc);
                long total = (long) Integer.bitCount(state.getInteger("Selected")) * Math.max(0, state.getInteger("MiningCeiling")) * 256;
                text(String.format(java.util.Locale.ROOT, "Scan: %.1f%%", 100.0 * state.getLong("Scanned") / Math.max(1, total)), 221, 126, 0xd6b77b);
            }
            text("Selected: " + Integer.bitCount(draft) + "/25", 112, 36, dirtySelection ? 0xe1b869 : 0xb4bdbc);
        }
        int rx = mouseX - guiLeft, ry = mouseY - guiTop;
        List<String> tip = tooltip(rx, ry);
        for (MetalButton button : controls) if (button.hovered(mouseX, mouseY)) tip = Arrays.asList(button.tooltip);
        if (tip != null) showTooltip(tip, mouseX, mouseY);
    }
    protected void showTooltip(List<String> lines, int mouseX, int mouseY) {
        int available = Math.max(mouseX - 16, width - mouseX - 16);
        int wrap = Math.max(48, Math.min(200, available));
        List<String> wrapped = new ArrayList<String>();
        for (String line : lines) if (line != null && !line.isEmpty()) wrapped.addAll(fontRendererObj.listFormattedStringToWidth(line, wrap));
        // Foreground drawing is translated by guiLeft/guiTop; vanilla tooltip bounds use screen coordinates.
        GL11.glPushMatrix();
        try { GL11.glTranslatef(-guiLeft, -guiTop, 0); drawHoveringText(wrapped, mouseX, mouseY, fontRendererObj); }
        finally { GL11.glPopMatrix(); }
    }
    protected List<String> tooltip(int x, int y) {
        if (container.tab == 2) {
            if (y >= 70 && y < 127 && x >= 78 && x < 240) {
                int slot = (y - 73) / 18 * 9 + (x - 78) / 18;
                if (slot >= 0 && slot < 25 && slot+container.filterPage*25 < ((dev.industrialtechnology.quarry.TileQuarry)container.machine).filter.getSizeInventory() && ((dev.industrialtechnology.quarry.TileQuarry) container.machine).filter.getStackInSlot(slot+container.filterPage*25) == null)
                    return Arrays.asList("Ghost filter icon", "Pick up an item from your inventory, then left-click here", "No item is consumed; right-click clears an entry");
            }
            if (y >= 29 && y < 48) return Arrays.asList("Matching mined drops will be permanently deleted instead of entering Quarry storage.", "Existing output items are never deleted.");
            return null;
        }
        if (container.tab != 0) return null;
        if (x >= gridX && x < gridX + cellSize * 5 && y >= gridY && y < gridY + cellSize * 5) {
            int cell = ((y - gridY) / cellSize) * 5 + (x - gridX) / cellSize;
            int cx = (container.machine.xCoord >> 4) + ChunkArea.dx(cell), cz = (container.machine.zCoord >> 4) + ChunkArea.dz(cell);
            boolean selected = (draft & (1 << cell)) != 0;
            String info = (state.getInteger("Completed") & (1 << cell)) != 0 ? "Scan complete" : selected ? "Selected" : "Not selected";
            return Arrays.asList("Chunk " + cx + ", " + cz, "X " + cx * 16 + ".." + (cx * 16 + 15), "Z " + cz * 16 + ".." + (cz * 16 + 15), info,
                cell == 12 ? "Machine chunk: always selected" : "Click to edit; Apply to save");
        }
        if (x >= 12 && x < 101 && y >= 35 && y < 69) {
            List<String> tip = new ArrayList<String>();
            tip.add(MachineStatus.values()[Math.max(0, Math.min(MachineStatus.values().length - 1, state.getInteger("Status")))].label());
            if (!state.getString("TicketError").isEmpty()) tip.add(state.getString("TicketError"));
            if (!state.getString("Issue").isEmpty()) tip.add(state.getString("Issue"));
            tip.add("Forced chunks: " + state.getInteger("Loaded"));
            if (container.machine.isQuarry() && !state.getBoolean("AutoLoad")) tip.add("Automatic loading disabled by server");
            return tip;
        }
        if (x >= 12 && x < 101 && y >= 71 && y < 118) return Arrays.asList(state.getInteger("Energy") + " / " + state.getInteger("Capacity") + " RF",
            state.getInteger("Input") + " RF/t (20-tick average)", "Cost: " + state.getInteger("Cost") + " RF per block");
        if (x >= 217 && x < 304 && y >= 80 && y < 138 && container.machine.isQuarry()) return Arrays.asList("Measured speed: " + state.getInteger("MeasuredSpeed") + " blocks/s (last 20 ticks)",
            String.format(java.util.Locale.ROOT, "Potential speed: %.1f blocks/s before server/terrain limits", state.getDouble("PotentialSpeed")), "Real blocks mined: " + state.getLong("Mined"),
            "Protected / unsupported: " + state.getLong("Skipped"), "Stored harvest batches: " + state.getInteger("Pending"), "Scan progress includes air and skipped blocks");
        if (x >= 110 && x < 208 && y >= 34 && y < 46) return Arrays.asList("Selected chunks: " + Integer.bitCount(draft) + " / 25",
            "Area: " + Integer.bitCount(draft) * 256 + " square blocks", "Server limit: " + state.getInteger("MaxChunks"), "Blue: selected; amber: current; green: complete");
        return null;
    }
    protected void text(String s, int x, int y, int color) { fontRendererObj.drawString(s, x, y, color); }
    private static String compact(int value) {
        if (value < 10000) return Integer.toString(value);
        return value >= 1000000 ? String.format(java.util.Locale.ROOT, "%.1fM", value / 1000000.0) : String.format(java.util.Locale.ROOT, "%.0fk", value / 1000.0);
    }
}
