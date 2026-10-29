import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import personal.factory.ProbeTransformer;

/** Verify real Minecraft bytecode in both MCP development and SRG production namespaces. */
public final class VerifyTimingHooks {
    public static void main(String[] args) throws Exception {
        Map<String,String> mappings = new HashMap<String,String>();
        for (String line : Files.readAllLines(Paths.get(args[1]))) {
            String[] parts = line.split(" ");
            if (parts.length == 5 && parts[0].equals("MD:")) {
                int slash = parts[1].lastIndexOf('/');
                mappings.put(parts[1].substring(0, slash) + "." + parts[1].substring(slash + 1) + parts[2], parts[3].substring(parts[3].lastIndexOf('/') + 1));
            }
        }
        String[] targets = {"net.minecraft.client.Minecraft", "net.minecraft.client.renderer.EntityRenderer",
            "net.minecraft.client.renderer.WorldRenderer", "net.minecraft.client.renderer.RenderGlobal", "net.minecraft.server.MinecraftServer"};
        // Minecraft has one tick, one display update, one limiter and two glFlush call sites.
        int[] expectedBegins = {5, 1, 1, 2, 1};
        try (JarFile jar = new JarFile(args[0])) {
            for (boolean srg : new boolean[]{false, true}) for (int index = 0; index < targets.length; index++) {
                String type = targets[index]; ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (InputStream input = jar.getInputStream(jar.getJarEntry(type.replace('.', '/') + ".class"))) {
                    byte[] chunk = new byte[16384]; int size; while ((size = input.read(chunk)) >= 0) buffer.write(chunk, 0, size);
                }
                byte[] original = buffer.toByteArray();
                if (srg) {
                    ClassWriter remapped = new ClassWriter(0);
                    new ClassReader(original).accept(new RemappingClassAdapter(remapped, new SimpleRemapper(mappings)), ClassReader.EXPAND_FRAMES);
                    original = remapped.toByteArray();
                }
                byte[] changed = new ProbeTransformer().transform(type, type, original);
                ClassNode node = new ClassNode(); new ClassReader(changed).accept(node, 0);
                int begins = 0, ends = 0;
                for (MethodNode method : node.methods) {
                    if (method.instructions.size() > 0) new Analyzer(new BasicVerifier()).analyze(node.name, method);
                    for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) instruction;
                        if (call.owner.equals("personal/factory/PhaseCapture")) {
                            if (call.name.equals("begin")) begins++; if (call.name.equals("end")) ends++;
                        }
                    }
                }
                if (begins != expectedBegins[index] || ends < begins) throw new AssertionError(type + " begins=" + begins + " ends=" + ends);
                System.out.println("PASS " + (srg ? "SRG " : "MCP ") + type + " begin=" + begins + " end=" + ends);
            }
        }
        try (JarFile previous = new JarFile(args[2]); JarFile current = new JarFile(args[3])) {
            List<String> unchanged = args.length > 4 && args[4].equals("allow-duct-change") ?
                Arrays.asList("personal/factory/PhaseCapture.class", "personal/factory/PhaseCaptureHooks.class", "personal/factory/FrameSpikeCapture.class", "personal/factory/LoadingPlugin.class") :
                Arrays.asList("personal/factory/DuctPacketFix.class", "personal/factory/LoadingPlugin.class");
            for (String type : unchanged) {
                if (!Arrays.equals(read(previous, type), read(current, type))) throw new AssertionError("Changed existing gameplay/filter code: " + type);
            }
        }
        System.out.println("Unchanged companion classes verified byte-identical to the selected baseline");
    }
    private static byte[] read(JarFile archive, String name) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream input = archive.getInputStream(archive.getJarEntry(name))) {
            byte[] bytes = new byte[4096]; int count; while ((count = input.read(bytes)) >= 0) out.write(bytes, 0, count);
        }
        return out.toByteArray();
    }
}
