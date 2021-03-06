// Copyright © 2013-2014 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.retrolambda.files;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class FilteringFileVisitor implements FileVisitor<Path> {

    private final Set<Path> fileFilter;
    private final FileVisitor<? super Path> target;

    public FilteringFileVisitor(Collection<Path> fileFilter, FileVisitor<Path> target) {
        this.fileFilter = new HashSet<>(fileFilter);
        this.target = target;
    }

    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return target.postVisitDirectory(dir, exc);
    }

    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return target.preVisitDirectory(dir, attrs);
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (fileFilter.contains(file)) {
            return target.visitFile(file, attrs);
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return target.visitFileFailed(file, exc);
    }
}
