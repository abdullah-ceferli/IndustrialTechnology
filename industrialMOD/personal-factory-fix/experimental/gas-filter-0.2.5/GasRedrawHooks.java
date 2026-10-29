package personal.factory;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class GasRedrawHooks {
    static byte[] transform(byte[] original) {
        if (original == null) return null;
        ClassNode node=new ClassNode();new ClassReader(original).accept(node,0);
        int hooks=0;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("func_147492_c") || !method.desc.equals("(IIILnet/minecraft/block/Block;I)Z")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call=(MethodInsnNode)instruction;
                if (!call.owner.equals("net/minecraft/world/World") || !(call.name.equals("setBlock") || call.name.equals("func_147465_d")) || !call.desc.equals("(IIILnet/minecraft/block/Block;II)Z")) continue;
                InsnList check=new InsnList();
                // Original flags stay on the operand stack as the helper's first
                // argument. Leave the original super.setBlock call intact.
                check.add(new VarInsnNode(Opcodes.ALOAD,0));
                for (int index=1;index<=3;index++) check.add(new VarInsnNode(Opcodes.ILOAD,index));
                check.add(new VarInsnNode(Opcodes.ALOAD,4));check.add(new VarInsnNode(Opcodes.ILOAD,5));
                check.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"personal/factory/GasRedrawFix","renderFlags","(ILjava/lang/Object;IIILjava/lang/Object;I)I",false));
                method.instructions.insertBefore(instruction,check);hooks++;
            }
        }
        if (hooks!=1) { System.err.println("[FactoryFix] Unsupported client block-update method; keeping original gas redraws");return original; }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        System.out.println("[FactoryFix] Added guarded gas render flags to WorldClient");return writer.toByteArray();
    }
}
