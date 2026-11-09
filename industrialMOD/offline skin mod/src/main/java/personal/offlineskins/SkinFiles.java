package personal.offlineskins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SkinFiles {
    private final File directory;
    public SkinFiles(File directory) { this.directory = directory; }
    public File directory() { return directory; }

    public File resolve(String filename) throws IOException {
        if (filename == null || filename.isEmpty() || filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0 || filename.indexOf(':') >= 0
                || !filename.toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
            throw new IOException("Choose a PNG filename from the skins folder.");
        }
        File file = new File(directory, filename);
        if (!file.getCanonicalFile().getParentFile().equals(directory.getCanonicalFile())
                || Files.isSymbolicLink(file.toPath())) {
            throw new IOException("Skin must be inside the skins folder.");
        }
        return file;
    }

    public List<String> list() {
        String[] names = directory.list((dir, name) -> name.toLowerCase(java.util.Locale.ROOT).endsWith(".png")
                && new File(dir, name).isFile() && !Files.isSymbolicLink(new File(dir, name).toPath()));
        if (names == null) return Collections.emptyList();
        Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
        return Arrays.asList(names);
    }
}
