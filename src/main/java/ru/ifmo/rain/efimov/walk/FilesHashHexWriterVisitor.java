package ru.ifmo.rain.efimov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class FilesHashHexWriterVisitor extends SimpleFileVisitor<Path> {
    private final BufferedWriter output;
    private FNVHash FNVHash;

    public FilesHashHexWriterVisitor(BufferedWriter output) {
        FNVHash = new FNVHash();
        this.output = output;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
        return writeResult(FNVHash.hashHex(file), file);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return writeResult(ru.ifmo.rain.efimov.walk.FNVHash.ERROR_HASH, file);
    }

    private FileVisitResult writeResult(String result, Path file) {
        try {
            output.write(result + ' ' + file.toString());
            output.newLine();
        } catch (IOException e) {
            return TERMINATE;
        }
        return CONTINUE;
    }
}