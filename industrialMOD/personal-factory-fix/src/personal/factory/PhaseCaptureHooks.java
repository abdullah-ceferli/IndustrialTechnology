package personal.factory;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Optional F9 elapsed-time scopes. Does not skip or reschedule game work. */
final class PhaseCaptureHooks {
    static byte[] transform(String type, byte[] original) {
        if (original == null) return null;
        boolean minecraft = type.equals("net.minecraft.client.Minecraft");
        boolean worldRenderer = type.equals("net.minecraft.client.renderer.WorldRenderer");
        boolean global = type.equals("net.minecraft.client.renderer.RenderGlobal");
        boolean camera = type.equals("net.minecraft.client.renderer.EntityRenderer");
        boolean server = type.equals("net.minecraft.server.MinecraftServer");
        if (!(minecraft || worldRenderer || global || camera || server)) return original;
        ClassNode node = new ClassNode();
        new ClassReader(original).accept(node, 0);
        int changed = 0;
        for (MethodNode method : node.methods) {
            int scope = -1;
            if (minecraft && named(method, "runTick", "func_71407_l") && method.desc.equals("()V")) scope = 0;
            if (camera && named(method, "updateCameraAndRender", "func_78480_b") && method.desc.equals("(F)V")) scope = 1;
            if (worldRenderer && named(method, "updateRenderer", "func_147892_a") && method.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;)V")) scope = 2;
            if (global && named(method, "sortAndRender", "func_72719_a") && method.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;ID)I")) scope = 3;
            if (global && named(method, "renderEntities", "func_147589_a") && method.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/culling/ICamera;F)V")) scope = 4;
            if (server && named(method, "tick", "func_71217_p") && method.desc.equals("()V")) scope = 5;
            if (scope >= 0) {
                // No new locals or control flow: keep the existing stack-map frames.
                method.instructions.insert(call("begin", scope));
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    int opcode = instruction.getOpcode();
                    if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                        method.instructions.insertBefore(instruction, call("end", scope));
                }
                PhaseCapture.registerHook(scope);
                changed++;
            }
            if (minecraft) for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode invoke = (MethodInsnNode) instruction;
                int displayScope = -1;
                if (invoke.owner.equals("org/lwjgl/opengl/Display")) {
                    displayScope = invoke.name.equals("update") && invoke.desc.equals("()V") ? 6 :
                        invoke.name.equals("sync") && invoke.desc.equals("(I)V") ? 7 : -1;
                }
                if (invoke.owner.equals("org/lwjgl/opengl/GL11") && invoke.name.equals("glFlush") && invoke.desc.equals("()V")) displayScope = 8;
                if (displayScope < 0) continue;
                method.instructions.insertBefore(instruction, call("begin", displayScope));
                method.instructions.insert(instruction, call("end", displayScope));
                PhaseCapture.registerHook(displayScope);
                changed++;
            }
        }
        if (changed == 0) {
            System.err.println("[FactoryProbe] No supported timing scopes in " + type + "; retaining original bytecode");
            return original;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        System.out.println("[FactoryProbe] Added F9 timing scopes to " + type);
        return writer.toByteArray();
    }

    private static boolean named(MethodNode method, String mcp, String srg) {
        return method.name.equals(mcp) || method.name.equals(srg);
    }

    private static InsnList call(String name, int scope) {
        InsnList result = new InsnList();
        result.add(new IntInsnNode(Opcodes.BIPUSH, scope));
        result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "personal/factory/PhaseCapture", name, "(I)V", false));
        return result;
    }
}
