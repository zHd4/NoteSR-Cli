/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipUtilsTest {
    @Test
    void testZipAndUnzipDirectory(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("file1.txt"), "Hello World");

        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(subDir.resolve("file2.txt"), "Subdir content");

        Path zipFile = Files.createTempFile("output", ".zip");
        ZipUtils.zipDirectory(tempDir.toString(), zipFile.toString());

        assertTrue(ZipUtils.isZipArchive(zipFile.toString()));

        Set<String> entries = ZipUtils.getTopLevelEntries(zipFile.toString());
        assertTrue(entries.contains("file1.txt"));
        assertTrue(entries.contains("sub/"));

        Path unzipTarget = Files.createTempDirectory("unzip-test");
        ZipUtils.unzip(zipFile.toString(), unzipTarget.toString());

        assertTrue(Files.exists(unzipTarget.resolve("file1.txt")));
        assertEquals("Hello World", Files.readString(unzipTarget.resolve("file1.txt")));

        assertTrue(Files.exists(unzipTarget.resolve("sub/file2.txt")));
        assertEquals("Subdir content", Files.readString(unzipTarget.resolve("sub/file2.txt")));
    }

    @Test
    void testIsZipArchiveWithNonZip(@TempDir Path tempDir) throws IOException {
        Path notZip = Files.writeString(tempDir.resolve("file.txt"), "Just some text");
        assertFalse(ZipUtils.isZipArchive(notZip.toString()));
    }

    @Test
    void testIsZipArchive(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("dummy.txt"), "data");
        Path zip = tempDir.resolve("output.zip");
        ZipUtils.zipDirectory(tempDir.toString(), zip.toString());

        assertTrue(ZipUtils.isZipArchive(zip.toString()));
    }

    @Test
    void testZipSlipProtection(@TempDir Path tempDir) throws IOException {
        Path zipFile = tempDir.resolve("bad.zip");

        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("../evil.txt");

            outputStream.putNextEntry(entry);
            outputStream.write("hacked".getBytes());
            outputStream.closeEntry();
        }

        Path extractTo = tempDir.resolve("unzipped");
        IOException ex = assertThrows(IOException.class, () ->
                ZipUtils.unzip(zipFile.toString(), extractTo.toString())
        );

        assertTrue(ex.getMessage().contains("Invalid record in archive"));
    }
}
