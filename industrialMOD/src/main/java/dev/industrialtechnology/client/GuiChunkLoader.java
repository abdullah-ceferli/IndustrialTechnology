package dev.industrialtechnology.client;

import java.util.List;
import java.util.Arrays;
import dev.industrialtechnology.machine.MachineContainer;
import dev.industrialtechnology.machine.MachineStatus;

/** Small, controls-only utility screen. No RF labels, energy bar, or inventory. */
public final class GuiChunkLoader extends GuiMachine {
    public GuiChunkLoader(MachineContainer container) { super(container); xSize = 206; ySize = 204; gridX = 63; gridY = 62; cellSize = 16; }
    @Override protected void configureButtons() {
        button(20, 12, 147, 54, 14, "1 Chunk", "Select only the machine chunk");
        button(21, 70, 147, 40, 14, "3x3", "Select the central nine chunks");
        button(22, 114, 147, 40, 14, "5x5", "Select all permitted chunks");
        button(23, 158, 147, 36, 14, "Reset", "Reset selection to the center");
        button(1, 12, 167, 57, 15, "Apply", "Apply the selected chunk set");
        button(0, 73, 167, 58, 15, "ON", "ON loads selected chunks without energy; OFF releases all owned tickets");
        button(24, 135, 167, 59, 15, "Preview", "Toggle outlines; automatically expire after 30 seconds");
        button(25, 12, 187, 182, 13, "Show Chunk Borders: ON", "Show loaded chunk borders locally; does not change chunk loading");
    }
    @Override protected void updateButtons() {
        for (MetalButton b : controls) {
            b.enabled = true; b.visible = true; b.selected = false;
            if (b.id == 0) { b.displayString = state.getBoolean("Enabled") ? "OFF" : "ON"; }
            if (b.id == 1) { b.selected = dirtySelection; b.enabled = dirtySelection && !waitingApply; }
            if (b.id == 24) { b.enabled = state.getBoolean("PreviewAllowed"); b.selected = AreaPreview.isActive(container.machine); }
            if (b.id == 25) b.displayString = "Show Chunk Borders: " + (OverlaySettings.loaders ? "ON" : "OFF");
        }
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) { drawPanel("chunk_loader", 256); drawGrid(); }
    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        text("Chunk Loader", 12, 11, 0xe3d7ba);
        int ordinal = Math.max(0, Math.min(MachineStatus.values().length - 1, state.getInteger("Status")));
        String status = "Status: " + MachineStatus.values()[ordinal].label();
        // Ticket errors wrap into their own tooltip; the compact status stays within the panel.
        text(ordinal == MachineStatus.CHUNK_LOADING_ERROR.ordinal() ? "Status: LOADING ERROR" : status, 12, 33, 0xd4c9ad);
        String count = "Selected: " + Integer.bitCount(draft) + " / 25";
        text(count, 12, 48, 0xb4bdbc);
        String loaded = "Loaded: " + state.getInteger("Loaded");
        text(loaded, xSize - 12 - fontRendererObj.getStringWidth(loaded), 48, 0xb4bdbc);
        int rx = x - guiLeft, ry = y - guiTop;
        List<String> tip = null;
        if (rx >= gridX && rx < gridX + 80 && ry >= gridY && ry < gridY + 80) tip = super.tooltip(rx, ry);
        else if (ry >= 29 && ry < 44) tip = Arrays.asList("Keeps selected chunks loaded without requiring energy.",
            "Owned forced chunks: " + state.getInteger("Loaded"), state.getString("TicketError"));
        for (MetalButton b : controls) if (b.hovered(x, y)) tip = Arrays.asList(b.tooltip);
        if (tip != null) showTooltip(tip, x, y);
    }
}
