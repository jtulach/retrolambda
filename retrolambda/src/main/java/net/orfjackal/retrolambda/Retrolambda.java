// Copyright © 2013-2014 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda;

import net.orfjackal.retrolambda.defaultmethods.Helpers;
import net.orfjackal.retrolambda.files.*;
import net.orfjackal.retrolambda.interfaces.ClassHierarchyAnalyzer;
import net.orfjackal.retrolambda.lambdas.*;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Retrolambda {
    /** Public method exposed to external users of the module to backport
     * content of a classfile into pre-JDK8 format. Primarilly eliminate
     * use of invokeDynamic instruction for all lambda invocations. Also
     * do something with interface static and defender methods.
     * 
     * @param bytecodeVersion version of bytecode to generate
     * @param classes class loader to use when loading other classes related to the 
     *   one being backported
     * @param arr the bytes of a classfile to backport
     * @return newly generated classes as well as updated content overriding
     *    already existing classes
     */
    
    public static Map<String, byte[]> reify(
        int bytecodeVersion, ClassLoader classes, byte[] arr
    ) {
        Map<String, byte[]> classFiles = new HashMap<>();
        ClassSaver saver = new ClassSaver(null) {
            @Override
            public void save(byte[] bytecode) throws IOException {
                if (bytecode == null) {
                    return;
                }
                ClassReader cr = new ClassReader(bytecode);
                classFiles.put(cr.getClassName(), bytecode);
            }
        };
        ClassHierarchyAnalyzer analyzer = new ClassHierarchyAnalyzer();
        Transformers transformers = new Transformers(bytecodeVersion, analyzer);
        LambdaClassSaver lambdaClassSaver = new LambdaClassSaver(saver, transformers);
        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        try (LambdaClassDumper dumper = new LambdaClassDumper(lambdaClassSaver)) {
            Thread.currentThread().setContextClassLoader(classes);
            dumper.install();

            analyzer.analyze(arr);

            for (ClassReader reader : analyzer.getInterfaces()) {
                byte[] companion = transformers.extractInterfaceCompanion(reader);
                ClassReader cr = new ClassReader(companion);
                classFiles.put(cr.getClassName(), companion);

                byte[] replace = transformers.backportInterface(reader);
                ClassReader rcr = new ClassReader(replace);
                classFiles.put(rcr.getClassName(), replace);
            }
            for (ClassReader reader : analyzer.getClasses()) {
                byte[] replace = transformers.backportClass(reader);
                ClassReader rcr = new ClassReader(replace);
                classFiles.put(rcr.getClassName(), replace);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(prev);
        }
        return classFiles;
    }
    

    public static void run(Config config) throws Throwable {
        int bytecodeVersion = config.getBytecodeVersion();
        Path inputDir = config.getInputDir();
        Path outputDir = config.getOutputDir();
        String classpath = config.getClasspath();
        List<Path> includedFiles = config.getIncludedFiles();
        System.out.println("Bytecode version: " + bytecodeVersion + " (" + config.getJavaVersion() + ")");
        System.out.println("Input directory:  " + inputDir);
        System.out.println("Output directory: " + outputDir);
        System.out.println("Classpath:        " + classpath);
        if (includedFiles != null) {
            System.out.println("Included files:   " + includedFiles.size());
        }

        if (!Files.isDirectory(inputDir)) {
            System.out.println("Nothing to do; not a directory: " + inputDir);
            return;
        }

        if (FeatureToggles.DEFAULT_METHODS == 1) {
            Helpers.config = config;
        }

        Thread.currentThread().setContextClassLoader(new NonDelegatingClassLoader(asUrls(classpath)));

        ClassHierarchyAnalyzer analyzer = new ClassHierarchyAnalyzer();
        ClassSaver saver = new ClassSaver(outputDir);
        Transformers transformers = new Transformers(bytecodeVersion, analyzer);
        LambdaClassSaver lambdaClassSaver = new LambdaClassSaver(saver, transformers);

        try (LambdaClassDumper dumper = new LambdaClassDumper(lambdaClassSaver)) {
            if (PreMain.isAgentLoaded()) {
                PreMain.setLambdaClassSaver(lambdaClassSaver);
            } else {
                dumper.install();
            }

            visitFiles(inputDir, includedFiles, new BytecodeFileVisitor() {
                @Override
                protected void visit(byte[] bytecode) {
                    analyzer.analyze(bytecode);
                }
            });

            List<byte[]> transformed = new ArrayList<>();
            for (ClassReader reader : analyzer.getInterfaces()) {
                transformed.add(transformers.extractInterfaceCompanion(reader));
                transformed.add(transformers.backportInterface(reader));
            }
            for (ClassReader reader : analyzer.getClasses()) {
                transformed.add(transformers.backportClass(reader));
            }

            // We need to load some of the classes (for calling the lambda metafactory)
            // so we need to take care not to modify any bytecode before loading them.
            for (byte[] bytecode : transformed) {
                saver.save(bytecode);
            }
        }
    }

    static void visitFiles(Path inputDir, List<Path> includedFiles, FileVisitor<Path> visitor) throws IOException {
        if (includedFiles != null) {
            visitor = new FilteringFileVisitor(includedFiles, visitor);
        }
        Files.walkFileTree(inputDir, visitor);
    }

    private static URL[] asUrls(String classpath) {
        String[] paths = classpath.split(System.getProperty("path.separator"));
        return Arrays.asList(paths).stream()
                .map(s -> Paths.get(s).toUri())
                .map(Retrolambda::uriToUrl)
                .toArray(URL[]::new);
    }

    private static URL uriToUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
