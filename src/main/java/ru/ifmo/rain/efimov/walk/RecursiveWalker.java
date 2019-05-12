package ru.ifmo.rain.efimov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalker {

    public void walk(Path inputFile, Path outputFile) throws IOException {
        try (BufferedReader input = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
             BufferedWriter output = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            for (String path; null != (path = input.readLine()); ) {
                walkPath(path, output);
            }
        }
    }

    protected void walkPath(String path, BufferedWriter output) throws IOException {
        try {
            Path current = Paths.get(path);
            FilesHashHexWriterVisitor visitor = new FilesHashHexWriterVisitor(output);
            Files.walkFileTree(current, visitor);
        } catch (InvalidPathException e) {
            output.write(FNVHash.ERROR_HASH + ' ' + path);
            output.newLine();
        }
    }
}
