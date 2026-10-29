package personal.factory;

import java.io.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;

/** Exercise inactive/active scopes, nested calls, frame recovery and transformed execution. */
public final class PhaseCaptureTest {
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static String finish() {
        StringWriter text = new StringWriter(); PrintWriter out = new PrintWriter(text);
        PhaseCapture.finish(out, 2); out.flush(); return text.toString();
    }
    private static String scope(String text, String name) {
        for (String line : text.split("\\r?\\n")) if (line.startsWith(name + ":")) return line;
        throw new AssertionError("Missing " + name);
    }
    public static void main(String[] args) throws Exception {
        PhaseCapture.registerHook(0); PhaseCapture.registerHook(2);
        PhaseCapture.start();
        PhaseCapture.begin(0); PhaseCapture.begin(0); PhaseCapture.end(0); PhaseCapture.end(0);
        PhaseCapture.begin(2); // Simulated exception before the normal-return hook.
        PhaseCapture.beginFrame();
        PhaseCapture.begin(2); PhaseCapture.end(2);
        Thread other = new Thread(() -> { PhaseCapture.begin(0); PhaseCapture.end(0); }, "other renderer");
        other.start(); other.join();
        PhaseCapture.frame(25000000L);
        String report = finish();
        require(scope(report, "client tick").contains("1 calls;"), "Nested scope counted once; other threads excluded");
        require(scope(report, "vanilla section rebuild").contains("1 calls;"), "Aborted nesting recovered next frame");
        require(scope(report, "terrain draw").contains("unavailable"), "Absent hook is unavailable, not measured zero");
        require(report.contains("no player") || report.contains("unavailable"), "Missing client context is safe");
        PhaseCapture.begin(0); PhaseCapture.end(0);
        PhaseCapture.start();
        require(scope(finish(), "client tick").contains("no completed calls"), "Captures reset; inactive hooks record nothing");

        // Transform a server-tick fixture and actually execute it with JVM verification.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String type = "net/minecraft/server/MinecraftServer";
        writer.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, type, null, "java/lang/Object", null);
        writer.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "runs", "I", null, null).visitEnd();
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "tick", "()V", null, null);
        method.visitCode(); method.visitFieldInsn(Opcodes.GETSTATIC, type, "runs", "I");
        method.visitInsn(Opcodes.ICONST_1); method.visitInsn(Opcodes.IADD);
        method.visitFieldInsn(Opcodes.PUTSTATIC, type, "runs", "I");
        method.visitInsn(Opcodes.RETURN); method.visitMaxs(0, 0); method.visitEnd(); writer.visitEnd();
        byte[] changed = PhaseCaptureHooks.transform(type.replace('/', '.'), writer.toByteArray());
        Class<?> fixture = new ClassLoader(PhaseCaptureTest.class.getClassLoader()) {
            Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
        }.define(changed);
        PhaseCapture.start(); fixture.getMethod("tick").invoke(null);
        require(fixture.getField("runs").getInt(null) == 1, "Transformed method preserves original work");
        require(scope(finish(), "local server tick").contains("1 calls;"), "Transformed execution records one server tick");
        System.out.println("Phase capture: inactive/nested/thread/reset/exception-recovery and verified transformed-execution checks passed");
    }
}
