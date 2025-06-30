package app.notesr.cli.compiler;

import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.util.ZipUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupCompilerTest {
    private static final String NOTESR_VERSION = "5.1";

    @TempDir
    private Path tempDir;

    private Path dbPath;
    private Path outputPath;
    private Path tempDirPath;

    @BeforeEach
    void setUp() {
        dbPath = getFixturePath("backup.db", tempDir);
        outputPath = tempDir.resolve("output.zip");
        tempDirPath = tempDir.resolve("output_temp");
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
        Path wrongDbPath = Path.of("not_path");
        BackupCompiler backupCompiler = new BackupCompiler(wrongDbPath, outputPath, NOTESR_VERSION);
        assertThrows(BackupIOException.class, backupCompiler::run);
    }
}
