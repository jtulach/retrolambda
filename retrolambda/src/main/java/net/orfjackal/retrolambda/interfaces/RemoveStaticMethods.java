// Copyright © 2013-2014 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda.interfaces;

import net.orfjackal.retrolambda.Flags;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

public class RemoveStaticMethods extends ClassVisitor {

    public RemoveStaticMethods(ClassVisitor next) {
        super(ASM5, next);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isStaticMethod(access)) {
            return null;
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    private static boolean isStaticMethod(int access) {
        return Flags.hasFlag(access, ACC_STATIC);
    }
}
