package dev.industrialtechnology.energy.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Observe energy writes in the installed HBM battery implementations; never replace their calculations. */
public final class BatteryMeterTransformer implements IClassTransformer,Opcodes {
    private static final String LEGACY="com.hbm.tileentity.machine.storage.TileEntityMachineBattery";
    private static final String SOCKET="com.hbm.tileentity.machine.storage.TileEntityBatterySocket";
    private static final String HOOK="dev/industrialtechnology/energy/BatteryFlowMeter";
    public byte[] transform(String name,String transformedName,byte[] original){
        if(original==null||!LEGACY.equals(transformedName)&&!SOCKET.equals(transformedName))return original;
        try{
            ClassNode c=new ClassNode();new ClassReader(original).accept(c,0);if(c.interfaces.contains("dev/industrialtechnology/energy/MeteredBattery"))return original;int count=0;
            for(Object object:c.methods){MethodNode m=(MethodNode)object;
                if(LEGACY.equals(transformedName)&&("setPower".equals(m.name)||"updateEntity".equals(m.name)||"func_145845_h".equals(m.name)))
                    for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof FieldInsnNode){FieldInsnNode f=(FieldInsnNode)n;
                        if(f.getOpcode()!=PUTFIELD||!f.owner.equals(c.name)||!f.name.equals("power")||!f.desc.equals("J"))continue;
                        int local=m.maxLocals;m.maxLocals+=5;InsnList before=new InsnList();
                        before.add(new VarInsnNode(LSTORE,local+1));before.add(new VarInsnNode(ASTORE,local));
                        before.add(new VarInsnNode(ALOAD,local));before.add(new FieldInsnNode(GETFIELD,f.owner,f.name,f.desc));before.add(new VarInsnNode(LSTORE,local+3));
                        before.add(new VarInsnNode(ALOAD,local));before.add(new VarInsnNode(LLOAD,local+1));m.instructions.insertBefore(n,before);
                        InsnList after=new InsnList();after.add(new VarInsnNode(ALOAD,local));after.add(new VarInsnNode(LLOAD,local+3));after.add(new VarInsnNode(LLOAD,local+1));
                        after.add(new MethodInsnNode(INVOKESTATIC,HOOK,"changed","(Ljava/lang/Object;JJ)V",false));m.instructions.insert(n,after);count++;
                    }
                if(SOCKET.equals(transformedName)&&"setPower".equals(m.name)&&"(J)V".equals(m.desc))
                    for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)n;
                        if(!call.owner.equals("api/hbm/energymk2/IBatteryItem")||!call.name.equals("setCharge")||!call.desc.equals("(Lnet/minecraft/item/ItemStack;J)V"))continue;
                        int local=m.maxLocals;m.maxLocals+=6;InsnList before=new InsnList();
                        before.add(new VarInsnNode(LSTORE,local+2));before.add(new VarInsnNode(ASTORE,local+1));before.add(new VarInsnNode(ASTORE,local));
                        before.add(new VarInsnNode(ALOAD,0));before.add(new MethodInsnNode(INVOKEVIRTUAL,c.name,"getPower","()J",false));before.add(new VarInsnNode(LSTORE,local+4));
                        before.add(new VarInsnNode(ALOAD,local));before.add(new VarInsnNode(ALOAD,local+1));before.add(new VarInsnNode(LLOAD,local+2));m.instructions.insertBefore(n,before);
                        InsnList after=new InsnList();after.add(new VarInsnNode(ALOAD,0));after.add(new VarInsnNode(LLOAD,local+4));after.add(new VarInsnNode(ALOAD,0));
                        after.add(new MethodInsnNode(INVOKEVIRTUAL,c.name,"getPower","()J",false));after.add(new MethodInsnNode(INVOKESTATIC,HOOK,"changed","(Ljava/lang/Object;JJ)V",false));m.instructions.insert(n,after);count++;
                    }
            }
            if(count==0)return original;
            c.interfaces.add("dev/industrialtechnology/energy/MeteredBattery");ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);
            System.out.println("[IndustrialTechnology] Battery flow observation: "+transformedName+" ("+count+" writes)");return out.toByteArray();
        }catch(RuntimeException failure){System.err.println("[IndustrialTechnology] Battery flow unavailable for "+transformedName+": "+failure);return original;}
    }
}
