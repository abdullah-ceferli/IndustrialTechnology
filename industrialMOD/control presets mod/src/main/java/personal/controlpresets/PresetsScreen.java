package personal.controlpresets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import org.lwjgl.input.Keyboard;

public final class PresetsScreen extends GuiScreen implements GuiYesNoCallback {
    private final GuiScreen parent;
    private final ControlsClient controls;
    private PresetStore store;
    private List<String> names = new ArrayList<String>();
    private GuiTextField name;
    private String selected;
    private String typed = "My controls";
    private String message = "";
    private String pending;
    private int page, rows;
    private boolean error;

    public PresetsScreen(GuiScreen parent, ControlsClient controls) { this.parent = parent; this.controls = controls; }
    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        if (name != null) typed = name.getText();
        buttonList.clear();
        try { store = controls.store(); names = store.names(); }
        catch (IOException ex) { store = null; message = ex.getMessage(); error = true; }
        rows = Math.max(1, Math.min(7, (height - 174) / 22));
        page = Math.max(0, Math.min(page, Math.max(0, (names.size() - 1) / rows)));
        int x = width / 2 - 150;
        for (int row = 0; row < rows && page * rows + row < names.size(); row++) {
            String item = names.get(page * rows + row);
            buttonList.add(new GuiButton(100 + row, x, 48 + row * 22, 300, 20,
                    fontRendererObj.trimStringToWidth((item.equals(selected) ? "> " : "") + item, 286)));
        }
        GuiButton previous = new GuiButton(4, x, 50 + rows * 22, 40, 20, "<");
        previous.enabled = page > 0; buttonList.add(previous);
        GuiButton next = new GuiButton(5, x + 260, 50 + rows * 22, 40, 20, ">");
        next.enabled = (page + 1) * rows < names.size(); buttonList.add(next);
        name = new GuiTextField(fontRendererObj, x, height - 102, 300, 18);
        name.setMaxStringLength(48); name.setText(typed);
        buttonList.add(new GuiButton(1, x, height - 78, 98, 20, "Save current"));
        buttonList.add(new GuiButton(2, x + 102, height - 78, 96, 20, "Load selected"));
        buttonList.add(new GuiButton(3, x + 202, height - 78, 98, 20, "Delete selected"));
        buttonList.add(new GuiButton(0, width / 2 - 75, height - 28, 150, 20, "Done"));
        updateButtons();
    }
    private void updateButtons() {
        for (Object item : buttonList) {
            GuiButton button = (GuiButton)item;
            if (button.id == 1) button.enabled = store != null && !name.getText().trim().isEmpty();
            if (button.id == 2 || button.id == 3) button.enabled = store != null && selected != null && names.contains(selected);
        }
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 0) { mc.displayGuiScreen(parent); return; }
        if (button.id == 4 || button.id == 5) { page += button.id == 4 ? -1 : 1; initGui(); return; }
        if (button.id >= 100) {
            selected = names.get(page * rows + button.id - 100);
            typed = selected; name.setText(typed); initGui(); return;
        }
        try {
            if (button.id == 1) {
                pending = PresetStore.validName(name.getText().trim());
                if (PresetStore.BEFORE_LOAD.equals(pending)) throw new IOException("Choose a different name for your preset.");
                if (store.contains(pending)) { mc.displayGuiScreen(new GuiYesNo(this, "Replace this saved preset?", pending, 1)); return; }
                save();
            } else if (button.id == 2) { message = controls.load(selected); error = false; }
            else if (button.id == 3) {
                pending = selected;
                mc.displayGuiScreen(new GuiYesNo(this, "Delete this saved preset?", pending, 3)); return;
            }
        } catch (IOException ex) { message = ex.getMessage(); error = true; }
        initGui();
    }
    private void save() throws IOException {
        store.save(pending, controls.capture());
        selected = pending; typed = pending; name.setText(typed);
        page = (store.names().indexOf(selected)) / rows;
        message = "Saved current controls as " + selected + "."; error = false;
    }
    @Override public void confirmClicked(boolean yes, int action) {
        if (yes) try {
            if (action == 1) save();
            if (action == 3) { store.delete(pending); selected = null; message = "Deleted preset."; error = false; }
        } catch (IOException ex) { message = ex.getMessage(); error = true; }
        mc.displayGuiScreen(this);
    }
    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Saved Controls", width / 2, 12, 0xffffff);
        drawCenteredString(fontRendererObj, "All key bindings + mouse settings", width / 2, 30, 0xcccccc);
        drawCenteredString(fontRendererObj, (page + 1) + " / " + Math.max(1, (names.size() + rows - 1) / rows), width / 2, 56 + rows * 22, 0xcccccc);
        drawString(fontRendererObj, "Preset name:", width / 2 - 150, height - 114, 0xcccccc);
        name.drawTextBox();
        drawCenteredString(fontRendererObj, fontRendererObj.trimStringToWidth(message, width - 16), width / 2, height - 49, error ? 0xff8888 : 0xaaffaa);
        super.drawScreen(mouseX, mouseY, partial);
    }
    @Override protected void mouseClicked(int x, int y, int button) {
        name.mouseClicked(x, y, button); super.mouseClicked(x, y, button);
    }
    @Override protected void keyTyped(char character, int code) {
        if (code == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(parent); return; }
        if (name.textboxKeyTyped(character, code)) updateButtons();
        else super.keyTyped(character, code);
    }
    @Override public void updateScreen() { name.updateCursorCounter(); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
}
