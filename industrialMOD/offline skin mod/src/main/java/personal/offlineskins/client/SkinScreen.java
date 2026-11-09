package personal.offlineskins.client;

import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;
import personal.offlineskins.OfflineSkins;

public final class SkinScreen extends GuiScreen {
    private final GuiScreen parent;
    private final ClientSkins skins = (ClientSkins) OfflineSkins.proxy;
    private List<String> names;
    private int page;
    private int rows;
    private String error;

    public SkinScreen(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        names = skins.files().list();
        rows = Math.max(1, Math.min(6, (height - 160) / 22));
        page = Math.max(0, Math.min(page, Math.max(0, (names.size() - 1) / rows)));
        buttonList.clear();
        buttonList.add(new GuiButton(7,width-74,8,66,20,"Friends"));
        int x = width / 2 - 150;
        for (int row = 0; row < rows && page * rows + row < names.size(); row++) {
            String name = names.get(page * rows + row);
            GuiButton button = new GuiButton(100 + row, x, 62 + row * 22, 220, 20,
                    (name.equals(skins.selected()) ? "> " : "") + fontRendererObj.trimStringToWidth(name, 200));
            buttonList.add(button);
        }
        buttonList.add(new GuiButton(1, x, height - 88, 98, 20, "Open skin folder"));
        buttonList.add(new GuiButton(2, x + 102, height - 88, 70, 20, "Refresh"));
        buttonList.add(new GuiButton(3, x + 176, height - 88, 124, 20, "Disable custom skin"));
        GuiButton back = new GuiButton(4, x, height - 64, 40, 20, "<");
        back.enabled = page > 0;
        buttonList.add(back);
        GuiButton next = new GuiButton(5, x + 260, height - 64, 40, 20, ">");
        next.enabled = (page + 1) * rows < names.size();
        buttonList.add(next);
        buttonList.add(new GuiButton(0, width / 2 - 70, height - 28, 140, 20, "Done"));
    }

    @Override protected void actionPerformed(GuiButton button) {
        error = null;
        try {
            if (button.id == 0) { mc.displayGuiScreen(parent); return; }
            if(button.id==7){mc.displayGuiScreen(new FriendsScreen(this));return;}
            if (button.id == 1) skins.openFolder();
            if (button.id == 2) skins.reload(false);
            if (button.id == 3) skins.select("");
            if (button.id == 4) page--;
            if (button.id == 5) page++;
            if (button.id >= 100) skins.select(names.get(page * rows + button.id - 100));
        } catch (IOException ex) { error = ex.getMessage(); }
        initGui();
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Offline Skins", width / 2, 12, 0xffffff);
        drawCenteredString(fontRendererObj, "Put skin PNGs in the skin folder, then click a filename.", width / 2, 30, 0xcccccc);
        drawCenteredString(fontRendererObj, "64x32 / 64x64 PNG - classic player model", width / 2, 43, 0xaaaaaa);
        if (names.isEmpty()) drawString(fontRendererObj, "No skins yet. Click Open skin folder.", width / 2 - 150, 65, 0xffdd88);
        if (skins.preview() != null) {
            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(skins.preview());
            // A flat front preview uses the same normalized texture as the player.
            int x = width / 2 + 106, y = 64;
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0);
            float scale = Math.min(1.0F, Math.max(0.25F, (height - 170) / 128.0F));
            GL11.glScalef(scale, scale, 1);
            x = 0;
            y = 0;
            rect(x + 8, y, 32, 32, 8, 8, 8, 8);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            rect(x + 8, y, 32, 32, 40, 8, 8, 8);
            GL11.glDisable(GL11.GL_BLEND);
            rect(x + 8, y + 32, 32, 48, 20, 20, 8, 12);
            rect(x - 8, y + 32, 16, 48, 44, 20, 4, 12);
            rect(x + 40, y + 32, 16, 48, 44, 20, 4, 12);
            rect(x + 8, y + 80, 16, 48, 4, 20, 4, 12);
            rect(x + 24, y + 80, 16, 48, 4, 20, 4, 12);
            GL11.glPopMatrix();
        }
        drawCenteredString(fontRendererObj, (page + 1) + " / " + Math.max(1, (names.size() + rows - 1) / rows), width / 2, height - 58, 0xcccccc);
        String message = error == null ? skins.status() : error;
        if (skins.lanSharing()) drawCenteredString(fontRendererObj, "LAN skin sharing is active.", width / 2, height - 116, 0xaaddff);
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(message, width - 16), width / 2, height - 103, error == null ? 0xaaffaa : 0xff8888);
        super.drawScreen(mouseX, mouseY, partial);
    }

    private void rect(int x, int y, int w, int h, int u, int v, int tw, int th) {
        // The Gui helper takes explicit texture dimensions and avoids a 256x256 assumption.
        net.minecraft.client.renderer.Tessellator t = net.minecraft.client.renderer.Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + h, zLevel, u / 64.0, (v + th) / 32.0);
        t.addVertexWithUV(x + w, y + h, zLevel, (u + tw) / 64.0, (v + th) / 32.0);
        t.addVertexWithUV(x + w, y, zLevel, (u + tw) / 64.0, v / 32.0);
        t.addVertexWithUV(x, y, zLevel, u / 64.0, v / 32.0);
        t.draw();
    }

    @Override protected void keyTyped(char character, int code) {
        if (code == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(character, code);
    }
}
