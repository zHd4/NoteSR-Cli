package app.notesr.cli.compiler;

import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.util.FileUtils;
import app.notesr.cli.util.ZipUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupCompilerTest {
    private static final String NOTESR_VERSION = "5.1";

    private Path dbPath;
    private Path outputPath;
    private Path tempDirPath;

    @BeforeEach
    void setUp() {
        String uuid = randomUUID().toString();

        dbPath = getFixturePath("backup.db");
        outputPath = getTempPath(uuid + ".zip");
        tempDirPath = getTempPath(uuid + "_temp");
    }

    @Test
    void testRun() throws IOException {
        BackupCompiler backupCompiler = new BackupCompiler(dbPath, outputPath, NOTESR_VERSION);

        backupCompiler.setTempDirPath(tempDirPath);
        backupCompiler.run();

        File outputArchive = outputPath.toFile();

        assertTrue(outputArchive.exists(), "The output archive not found");
        assertTrue(ZipUtils.isZipArchive(outputArchive.getAbsolutePath()), "The output file is not archive");
    }

    @Test
    void testRunWhenDbFileDoesNotExists() {
        Path wrongDbPath = getTempPath(randomUUID().toString());
        BackupCompiler backupCompiler = new BackupCompiler(wrongDbPath, outputPath, NOTESR_VERSION);

        assertThrows(BackupIOException.class, backupCompiler::run);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDirPath != null && Files.exists(tempDirPath)) {
            FileUtils.deleteDir(tempDirPath);
        }

        if (outputPath != null && Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
    }
}
