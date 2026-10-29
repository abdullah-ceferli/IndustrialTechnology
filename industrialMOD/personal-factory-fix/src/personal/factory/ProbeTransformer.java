package personal.factory;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Observe the vanilla client renderer; never suppress updates or alter world state. */
public final class ProbeTransformer implements IClassTransformer {
    public byte[] transform(String name, String mapped, byte[] bytes) {
        bytes = PhaseCaptureHooks.transform(mapped, bytes);
        boolean regions = "net.minecraft.client.renderer.RenderGlobal".equals(mapped);
        boolean angelicaRegions = "org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer".equals(mapped);
        boolean tiles = "net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher".equals(mapped);
        if ("cofh.core.network.PacketTile".equals(mapped) || "cofh.thermaldynamics.block.TileTDBase".equals(mapped))
            return patchDuctPackets(mapped, bytes);
        if (bytes == null || (!regions && !angelicaRegions && !tiles)) return bytes;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int patched = 0;
        for (MethodNode method : node.methods) {
            if ((regions && method.desc.equals("(IIIIII)V") &&
                (method.name.equals("markBlocksForUpdate") || method.name.equals("func_72725_b")))
                || (angelicaRegions && method.name.equals("scheduleRebuildForBlockArea") && method.desc.equals("(IIIIIIZ)V"))) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                for (int i = 1; i <= 6; i++) hook.add(new VarInsnNode(Opcodes.ILOAD, i));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "personal/factory/FactoryProbe", "region", "(Ljava/lang/Object;IIIIII)V", false));
                method.instructions.insert(hook);
                patched++;
            }
            if (tiles && (method.name.equals("renderTileEntityAt") || method.name.equals("func_147549_a")) &&
                method.desc.endsWith(";DDDF)V")) {
                InsnList start = new InsnList();
                start.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "personal/factory/FactoryProbe", "beginRender", "()V", false));
                method.instructions.insert(start);
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.RETURN) {
                        InsnList end = new InsnList();
                        end.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        end.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "personal/factory/FactoryProbe", "endRender", "(Ljava/lang/Object;)V", false));
                        method.instructions.insertBefore(instruction, end);
                    }
                }
                patched++;
            }
        }
        if (patched != 1) {
            if (angelicaRegions) {
                System.err.println("[FactoryProbe] Unsupported Angelica rebuild method; keeping original class");
                return bytes;
            }
            throw new IllegalStateException("FactoryProbe expected one hook in " + mapped + ", found " + patched);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        System.out.println("[FactoryProbe] Instrumented " + mapped);
        return writer.toByteArray();
    }
    private byte[] patchDuctPackets(String mapped,byte[] bytes) {
        if(bytes==null)return null;
        boolean packet=mapped.equals("cofh.core.network.PacketTile");
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int begins=0,marks=0,infoInvalidations=0;
        for(MethodNode method:node.methods) {
            if(!packet && method.name.equals("handleTileInfoPacket") && method.desc.equals("(Lcofh/core/network/PacketCoFHBase;ZLnet/minecraft/entity/player/EntityPlayer;)V")) {
                InsnList invalidate=new InsnList();
                invalidate.add(new VarInsnNode(Opcodes.ALOAD,0));
                invalidate.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"personal/factory/DuctPacketFix","invalidate","(Ljava/lang/Object;)V",false));
                method.instructions.insert(invalidate);infoInvalidations++;
            }
            if(!(packet?method.name.equals("handlePacket"):method.name.equals("handleTilePacket")))continue;
            for(AbstractInsnNode instruction:method.instructions.toArray()) {
                if(instruction instanceof MethodInsnNode) {
                    MethodInsnNode call=(MethodInsnNode)instruction;
                    if(packet && call.owner.equals("cofh/core/network/ITilePacketHandler") && call.name.equals("handleTilePacket")) {
                        InsnList start=new InsnList();
                        start.add(new VarInsnNode(Opcodes.ALOAD,3));
                        start.add(new VarInsnNode(Opcodes.ALOAD,0));
                        start.add(new VarInsnNode(Opcodes.ILOAD,2));
                        start.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"personal/factory/DuctPacketFix","beginPacket","(Ljava/lang/Object;Ljava/lang/Object;Z)V",false));
                        method.instructions.insertBefore(call,start);begins++;
                    }
                    if(call.owner.equals("net/minecraft/world/World") && call.desc.equals("(III)V") &&
                        (call.name.equals("markBlockForUpdate")||call.name.equals("func_147471_g"))) {
                        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,"personal/factory/DuctPacketFix",packet?"outerMark":"innerMark","(Ljava/lang/Object;III)V",false));marks++;
                    }
                }
                if(packet && instruction.getOpcode()==Opcodes.RETURN) {
                    method.instructions.insertBefore(instruction,new MethodInsnNode(Opcodes.INVOKESTATIC,"personal/factory/DuctPacketFix","endPacket","()V",false));
                }
            }
        }
        if(marks!=1 || (packet && begins!=1) || (!packet && infoInvalidations!=1)) {
            System.err.println("[FactoryFix] Unsupported "+mapped+" bytecode; keeping original class");return bytes;
        }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);
        System.out.println("[FactoryFix] Patched redundant redraw handling in "+mapped);
        return writer.toByteArray();
    }
}
