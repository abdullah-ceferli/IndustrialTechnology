package personal.controlpresets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Only this mod's preset file is read/written here; no options or mod configs. */
public final class PresetStore {
    public static final String BEFORE_LOAD = "Before last load";
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int MAX_BYTES = 1024 * 1024;
    private final Path file;
    private Document document = new Document();

    public static final class Binding {
        public String category;
        public String description;
        public int occurrence;
        public int keyCode;
        public Binding() { }
        public Binding(String category, String description, int occurrence, int keyCode) {
            this.category = category; this.description = description;
            this.occurrence = occurrence; this.keyCode = keyCode;
        }
        public String id() { return category.length() + ":" + category + description.length() + ":" + description + ":" + occurrence; }
    }
    public static final class Snapshot {
        public List<Binding> bindings = new ArrayList<Binding>();
        public float sensitivity;
        public boolean invertMouse;
        public boolean touchscreen;
    }
    private static final class Document {
        int version = 1;
        Map<String, Snapshot> presets = new LinkedHashMap<String, Snapshot>();
    }

    public PresetStore(File directory) { file = new File(directory, "presets.json").toPath(); }
    public Path file() { return file; }

    public void read() throws IOException {
        if (!Files.exists(file)) { document = new Document(); return; }
        if (Files.size(file) > MAX_BYTES) throw new IOException("Preset file is too large (maximum 1 MiB).");
        try {
            Document loaded = JSON.fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), Document.class);
            if (loaded == null || loaded.version != 1 || loaded.presets == null || loaded.presets.size() > 50)
                throw new IOException("Invalid or unsupported preset file. Existing file was kept.");
            for (Map.Entry<String, Snapshot> entry : loaded.presets.entrySet()) {
                validName(entry.getKey());
                validate(entry.getValue());
            }
            document = loaded;
        } catch (JsonParseException | IllegalStateException ex) {
            throw new IOException("Cannot read presets.json. Existing file was kept.", ex);
        }
    }

    public List<String> names() { return new ArrayList<String>(document.presets.keySet()); }
    public boolean contains(String name) { return document.presets.containsKey(name); }
    public Snapshot get(String name) throws IOException {
        Snapshot result = document.presets.get(name);
        if (result == null) throw new IOException("Choose a saved preset first.");
        return copy(result);
    }

    public void save(String name, Snapshot snapshot) throws IOException {
        name = validName(name);
        if (BEFORE_LOAD.equals(name)) throw new IOException("That name is reserved for the automatic backup.");
        put(name, snapshot);
    }
    public void saveBeforeLoad(Snapshot snapshot) throws IOException { put(BEFORE_LOAD, snapshot); }
    private void put(String name, Snapshot snapshot) throws IOException {
        validate(snapshot);
        Document changed = duplicateDocument();
        if (!changed.presets.containsKey(name) && changed.presets.size() >= 50)
            throw new IOException("Maximum 50 presets. Delete an old preset first.");
        changed.presets.put(name, copy(snapshot));
        write(changed);
        document = changed;
    }
    public void delete(String name) throws IOException {
        Document changed = duplicateDocument();
        if (changed.presets.remove(name) == null) throw new IOException("Preset no longer exists.");
        write(changed);
        document = changed;
    }
    private Document duplicateDocument() {
        Document changed = new Document();
        changed.presets.putAll(document.presets);
        return changed;
    }
    private void write(Document changed) throws IOException {
        byte[] bytes = JSON.toJson(changed).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IOException("Preset file would exceed 1 MiB.");
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "presets-", ".tmp");
        try {
            Files.write(temporary, bytes);
            if (Files.exists(file)) Files.copy(file, file.resolveSibling("presets.json.bak"), StandardCopyOption.REPLACE_EXISTING);
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public static String validName(String name) throws IOException {
        if (name == null || name.trim().isEmpty() || name.length() > 48 || !name.equals(name.trim()))
            throw new IOException("Use a preset name of 1-48 characters, with no outer spaces.");
        for (int i = 0; i < name.length(); i++) if (Character.isISOControl(name.charAt(i)))
            throw new IOException("Preset names cannot contain control characters.");
        return name;
    }
    private static void validate(Snapshot value) throws IOException {
        if (value == null || value.bindings == null || value.bindings.isEmpty() || value.bindings.size() > 4096
                || Float.isNaN(value.sensitivity) || value.sensitivity < 0 || value.sensitivity > 1)
            throw new IOException("Invalid control settings in preset. Existing file was kept.");
        Set<String> seen = new HashSet<String>();
        for (Binding key : value.bindings) {
            if (key == null || key.category == null || key.description == null || key.description.isEmpty()
                    || key.category.length() > 512 || key.description.length() > 512 || key.occurrence < 0
                    || key.occurrence > 4095 || key.keyCode < -100 || key.keyCode > 65535 || !seen.add(key.id()))
                throw new IOException("Invalid or duplicate key binding in preset. Existing file was kept.");
        }
    }
    private static Snapshot copy(Snapshot source) {
        Snapshot result = new Snapshot();
        result.sensitivity = source.sensitivity; result.invertMouse = source.invertMouse; result.touchscreen = source.touchscreen;
        for (Binding key : source.bindings)
            result.bindings.add(new Binding(key.category, key.description, key.occurrence, key.keyCode));
        return result;
    }
}
