/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Wiper {
    private static final int LOOPS_COUNT = 6;

    public static void wipeDir(File dir) throws IOException {
        if (dir == null) {
            throw new NullPointerException("dir is null");
        }

        if (!dir.isDirectory()) {
            throw new IOException(dir.getAbsolutePath() + " is not a directory");
        }

        try (Stream<Path> paths = Files.list(dir.toPath())) {
            paths.forEach(path -> {
                try {
                    if (Files.exists(path)) {
                        if (Files.isDirectory(path)) {
                            wipeDir(path.toFile());
                        } else {
                            wipeFile(path.toFile());
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        Files.delete(dir.toPath());
    }

    public static void wipeFile(File file) throws IOException {
        for (int i = 0; i < LOOPS_COUNT; i++) {
            wipeFileData(file);
        }

        Files.delete(file.toPath());
    }

    private static void wipeFileData(File file) throws IOException {
        long fileSize = file.length();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            try {
                stream.write(new byte[(int) fileSize]);
            } catch (OutOfMemoryError e) {
                long bytesWrite = 0;

                do {
                    try {
                        byte[] empty = new byte[(int) (getAvailableMemory() / 2)];

                        stream.write(empty);
                        bytesWrite += empty.length;
                    } catch (OutOfMemoryError ex) {
                        log.warn(ex.getMessage());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                } while (bytesWrite < fileSize);
            }
        }
    }

    private static long getAvailableMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
    }
}
