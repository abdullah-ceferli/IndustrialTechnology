package personal.controlpresets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class PresetStoreTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private PresetStore.Snapshot layout() {
        PresetStore.Snapshot result = new PresetStore.Snapshot();
        result.sensitivity = 0.73F; result.invertMouse = true;
        result.bindings.add(new PresetStore.Binding("movement", "key.forward", 0, 17));
        result.bindings.add(new PresetStore.Binding("gameplay", "key.attack", 0, -100));
        result.bindings.add(new PresetStore.Binding("gameplay", "key.use", 0, -99));
        result.bindings.add(new PresetStore.Binding("a.mod", "key.optional", 0, 0));
        return result;
    }
    @Test public void persistsKeyboardMouseUnboundAndSettings() throws Exception {
        PresetStore store = new PresetStore(temporary.getRoot());
        store.read(); store.save("Mənim controls", layout());
        PresetStore reopened = new PresetStore(temporary.getRoot()); reopened.read();
        PresetStore.Snapshot actual = reopened.get("Mənim controls");
        assertEquals(4, actual.bindings.size());
        assertEquals(-100, actual.bindings.get(1).keyCode);
        assertEquals(-99, actual.bindings.get(2).keyCode);
        assertEquals(0, actual.bindings.get(3).keyCode);
        assertEquals(0.73F, actual.sensitivity, 0.00001F);
        assertTrue(actual.invertMouse);
    }
    @Test public void retainedSnapshotsAreIndependentAndWritesKeepBackup() throws Exception {
        PresetStore store = new PresetStore(temporary.getRoot());
        PresetStore.Snapshot saved = layout(); store.save("Original", saved);
        saved.bindings.get(0).keyCode = 20;
        assertEquals(17, store.get("Original").bindings.get(0).keyCode);
        byte[] before = Files.readAllBytes(store.file());
        store.saveBeforeLoad(saved);
        assertArrayEquals(before, Files.readAllBytes(store.file().resolveSibling("presets.json.bak")));
        assertEquals(20, store.get(PresetStore.BEFORE_LOAD).bindings.get(0).keyCode);
        PresetStore.Snapshot returned = store.get("Original"); returned.bindings.clear();
        assertEquals(4, store.get("Original").bindings.size());
    }
    @Test public void brokenFileIsNotOverwritten() throws Exception {
        PresetStore store = new PresetStore(temporary.getRoot());
        byte[] corrupt = "{ definitely not json".getBytes(StandardCharsets.UTF_8);
        Files.write(store.file(), corrupt);
        try { store.read(); fail("Expected broken-file error"); } catch (IOException expected) { }
        assertArrayEquals(corrupt, Files.readAllBytes(store.file()));
    }
    @Test public void validatesBeforeReplacingValidFile() throws Exception {
        PresetStore store = new PresetStore(temporary.getRoot()); store.save("Good", layout());
        byte[] good = Files.readAllBytes(store.file());
        PresetStore.Snapshot invalid = layout(); invalid.bindings.get(0).keyCode = -101;
        try { store.save("Bad", invalid); fail(); } catch (IOException expected) { }
        invalid = layout(); invalid.bindings.add(invalid.bindings.get(0));
        try { store.save("Bad", invalid); fail(); } catch (IOException expected) { }
        try { store.save("Bad\nname", layout()); fail(); } catch (IOException expected) { }
        try { store.save(PresetStore.BEFORE_LOAD, layout()); fail(); } catch (IOException expected) { }
        assertArrayEquals(good, Files.readAllBytes(store.file()));
    }
    @Test public void stableIdentitySeparatesCategoriesAndDuplicateNames() {
        PresetStore.Binding first = new PresetStore.Binding("a", "bc", 0, 1);
        PresetStore.Binding second = new PresetStore.Binding("ab", "c", 0, 1);
        PresetStore.Binding duplicate = new PresetStore.Binding("a", "bc", 1, 1);
        assertNotEquals(first.id(), second.id()); assertNotEquals(first.id(), duplicate.id());
    }
    @Test public void deletingPersistsAndUnknownVersionsAreRejected() throws Exception {
        PresetStore store = new PresetStore(temporary.getRoot()); store.save("Delete me", layout());
        store.delete("Delete me");
        PresetStore reopened = new PresetStore(temporary.getRoot()); reopened.read();
        assertFalse(reopened.contains("Delete me"));
        Path file = store.file();
        Files.write(file, "{\"version\":999,\"presets\":{}}".getBytes(StandardCharsets.UTF_8));
        try { reopened.read(); fail(); } catch (IOException expected) { }
    }
}
