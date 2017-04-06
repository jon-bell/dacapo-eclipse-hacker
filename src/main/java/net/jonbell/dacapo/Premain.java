package net.jonbell.dacapo;

import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Premain {
	public static String getPropertyHideBootClasspath(String prop)
	{
		if(prop.equals("sun.boot.class.path"))
			return null;
		else if(prop.equals("os.name"))
			return "linux";
		return System.getProperty(prop);
	}
	public static Object getClassPath(String v, Class c) {
		try {
			Constructor cons = c.getConstructor(String.class);
			return cons.newInstance(System.getProperty("eclipse.java.home") + "/lib/rt.jar");
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void premain(String args, Instrumentation inst) {
		inst.addTransformer(new ClassFileTransformer() {

			@Override
			public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
					@Override
					public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
						MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
						return new MethodVisitor(Opcodes.ASM5, mv) {
							@Override
							public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
								if (name.equals("getProperty") && className.equals("org/eclipse/jdt/core/tests/util/Util")) {
									owner = "net/jonbell/dacapo/Premain";
									name = "getPropertyHideBootClasspath";
								}
								else if (owner.equals("org/eclipse/jdt/core/JavaCore") && name.equals("getClasspathVariable")) {
									mv.visitLdcInsn("org.eclipse.core.runtime.Path");
									mv.visitInsn(Opcodes.ICONST_0);
									mv.visitLdcInsn(className.replace("/", "."));
									mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
									mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
									mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false);

									mv.visitMethodInsn(INVOKESTATIC, "net/jonbell/dacapo/Premain", "getClassPath", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", false);
									mv.visitTypeInsn(CHECKCAST, "org/eclipse/core/runtime/Path");
									return;
								}
								super.visitMethodInsn(opcode, owner, name, desc, itf);
							}
						};
					}
				};
				ClassReader cr = new ClassReader(classfileBuffer);
				cr.accept(cv, 0);
				return cw.toByteArray();
			}
		});
	}
}
