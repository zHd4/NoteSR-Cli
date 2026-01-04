/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;

class WiperTest {
    private static final Random RANDOM = new Random();

    private static final int MIN_FILE_SIZE = 1024;
    private static final int MAX_FILE_SIZE = 1024 * 10;

    @TempDir
    private Path tempDir;

    @Test
    void testWipeFile() throws IOException {
        Path testFilePath = tempDir.resolve("temp_file");
        byte[] testFileContent = new byte[RANDOM.nextInt(MIN_FILE_SIZE, MAX_FILE_SIZE)];

        RANDOM.nextBytes(testFileContent);
        Files.write(testFilePath, testFileContent);

        File testFile = testFilePath.toFile();
        Wiper.wipeFile(testFile);

        assertFalse(testFile.exists(), "File must be wiped");
    }

    @Test
    void testWipeDir() throws IOException {
        Path testDirPath = tempDir.resolve("temp_dir");
        Path testFilePath = testDirPath.resolve("test_file");

        byte[] testFileContent = new byte[RANDOM.nextInt(MIN_FILE_SIZE, MAX_FILE_SIZE)];
        RANDOM.nextBytes(testFileContent);

        File testDir = testDirPath.toFile();
        File testFile = testFilePath.toFile();

        Files.createDirectory(testDirPath);
        Files.write(testFile.toPath(), testFileContent);

        Wiper.wipeDir(testDir);

        assertFalse(testDir.exists(), "Dir must be wiped");
        assertFalse(testFile.exists(), "File must be wiped");
    }
}
