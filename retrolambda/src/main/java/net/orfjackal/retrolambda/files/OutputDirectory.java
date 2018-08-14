// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda.files;

import net.orfjackal.retrolambda.ext.ow2asm.EnhancedClassReader;

import java.io.IOException;
import java.nio.file.*;

public class OutputDirectory {

    private final Path outputDir;

    public OutputDirectory(Path outputDir) {
        this.outputDir = outputDir;
    }

    public void writeClass(byte[] bytecode, boolean isJavacHacksEnabled) throws IOException {
        if (bytecode == null) {
            return;
        }
        EnhancedClassReader cr = new EnhancedClassReader(bytecode, isJavacHacksEnabled);
        Path relativePath = outputDir.getFileSystem().getPath(cr.getClassName() + ".class");
        writeFile(relativePath, bytecode);
    }

    public void writeFile(Path relativePath, byte[] content) throws IOException {
        Path outputFile = outputDir.resolve(relativePath);
        Files.createDirectories(outputFile.getParent());
        Files.write(outputFile, content);
    }
}
