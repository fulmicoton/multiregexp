package com.fulmicoton.multiregexp;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CreateCodePointGetter implements Opcodes {


    private static int nbCreatedClass = 0;

    public static Class loadClass(final String className, byte[] b) {
        //override classDefine (as it is protected) and define the class.
        Class clazz = null;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            Class cls = Class.forName("java.lang.ClassLoader");
            java.lang.reflect.Method method =
                    cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });

            // protected method invocaton
            method.setAccessible(true);
            try {
                Object[] args = new Object[] { className, b, new Integer(0), new Integer(b.length)};
                clazz = (Class) method.invoke(loader, args);
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return clazz;
    }

    public synchronized static CodePointGetter createCodePointGetter(final char[] points)  {
        try {
            nbCreatedClass += 1;

            ClassWriter cw = new ClassWriter(0);
            FieldVisitor fv;
            MethodVisitor mv;
            AnnotationVisitor av0;

            final String classNameWithSlash = "com/fulmicoton/multiregexp/CodePointGetter" + nbCreatedClass;
            cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, classNameWithSlash, null, "java/lang/Object", new String[] { "com/fulmicoton/multiregexp/CodePointGetter" });

            {   // Constructor
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {   // Actual method
                mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "getCodePoint", "(C)I", null, null);
                mv.visitCode();


                int resVal = 0;
                for (final char point: points) {
                    mv.visitVarInsn(ILOAD, 1); // get the first argument
                    mv.visitIntInsn(BIPUSH, (int)point);

                    Label label = new Label();
                    mv.visitJumpInsn(IF_ICMPGE, label); // Greater or equal ---> jump
                    mv.visitIntInsn(SIPUSH, resVal);
                    mv.visitInsn(IRETURN);
                    resVal += 1;
                    mv.visitLabel(label);
                    mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

                }
                mv.visitIntInsn(SIPUSH, resVal);
                mv.visitInsn(IRETURN);
                mv.visitMaxs(2, 4);
                mv.visitEnd();

            }
            cw.visitEnd();

            final byte[] classByte = cw.toByteArray();
            return (CodePointGetter)loadClass(classNameWithSlash.replace("/", "."), classByte).newInstance();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        CodePointGetter codePointGetter = createCodePointGetter(new char[]{'b', 'd'});
        System.out.println(codePointGetter.getCodePoint('a'));
        System.out.println(codePointGetter.getCodePoint('b'));
        System.out.println(codePointGetter.getCodePoint('c'));
        System.out.println(codePointGetter.getCodePoint('d'));

    }
}
