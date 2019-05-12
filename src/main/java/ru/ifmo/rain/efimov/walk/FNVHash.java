package ru.ifmo.rain.efimov.walk;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FNVHash {
    public static final String ERROR_HASH = "00000000";
    public static final int START_HASH = 0x811c9dc5;

    public String hashHex(Path filePath) {
        try {
            int hash = hash32((filePath));
            return String.format("%08x", hash);
        } catch (Exception e) {
            return ERROR_HASH;
        }
    }

    public int hash32(Path filePath) throws IOException {
        int hash = START_HASH;
        try (InputStream file = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            for (int bytesReaded; (bytesReaded = file.read(buffer)) != -1; )
                hash = hash32(hash, buffer, bytesReaded);
        }
        return hash;
    }

    private int hash32(int hash, final byte[] buffer, int bytesNumber) {
        for (int i = 0; i < bytesNumber; i++) {
            final byte b = buffer[i];
            hash = (hash * 0x01000193) ^ (b & 0xff);
        }
        return hash;
    }
}
