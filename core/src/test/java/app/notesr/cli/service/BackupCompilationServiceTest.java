package app.notesr.cli.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupCompilationServiceTest {
    @TempDir
    private Path tempDir;

    @Test
    void testCompile() throws Exception {
        File dbFile = getFixturePath("backup.db").toFile();
        File outputFile = Files.createFile(tempDir.resolve("output.zip")).toFile();

        BackupCompilationService service = new BackupCompilationService();
        Path actualPath = service.compile(dbFile, outputFile, "5.1");

        assertNotNull(actualPath, "Actual path is null");
        assertTrue(Files.exists(actualPath), "Actual path not found");
        assertTrue(Files.isDirectory(actualPath), "Actual path must be a file");
    }
}
