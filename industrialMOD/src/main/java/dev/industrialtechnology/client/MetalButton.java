package dev.industrialtechnology.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Flat pixel bevels, with restrained amber selection. */
public final class MetalButton extends GuiButton {
    public boolean selected;
    public final String tooltip;
    public MetalButton(int id, int x, int y, int w, int h, String label, String tooltip) {
        super(id, x, y, w, h, label); this.tooltip = tooltip;
    }
    @Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
        int fill = !enabled ? 0xff282b2e : selected ? 0xff66513a : field_146123_n ? 0xff52595c : 0xff383e42;
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xff14191d);
        drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, fill);
        drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + 2, selected ? 0xffb18a4e : 0xff697175);
        mc.fontRenderer.drawString(displayString, xPosition + (width - mc.fontRenderer.getStringWidth(displayString)) / 2,
            yPosition + (height - 8) / 2, enabled ? (selected ? 0xffe5bd75 : 0xffd0d4d2) : 0xff727a7d);
    }
    public boolean hovered(int x, int y) { return visible && x >= xPosition && y >= yPosition && x < xPosition + width && y < yPosition + height; }
}
