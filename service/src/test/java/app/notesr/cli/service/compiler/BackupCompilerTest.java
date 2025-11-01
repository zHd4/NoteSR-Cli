package app.notesr.cli.service.compiler;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupIOException;
import app.notesr.cli.core.util.ZipUtils;
import app.notesr.cli.service.compiler.BackupCompiler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;

import static app.notesr.cli.core.util.test.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupCompilerTest {
    private static final String NOTESR_VERSION = "5.2.3";
    private static final int KEY_SIZE = 48;

    @TempDir
    private Path tempDir;

    private Path dbPath;
    private Path outputPath;
    private CryptoSecrets secrets;

    @BeforeEach
    void setUp() throws Exception {
        dbPath = getFixturePath("shared/backup.db", tempDir);
        outputPath = tempDir.resolve("output.zip");

        byte[] keyBytes = new byte[KEY_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(keyBytes);

        secrets = new CryptoSecrets(keyBytes);
    }

    @Test
    void testRun() throws Exception {
        BackupCompiler backupCompiler = new BackupCompiler(dbPath, outputPath, secrets, NOTESR_VERSION);
        backupCompiler.run();

        File outputArchive = outputPath.toFile();

        assertTrue(outputArchive.exists(), "The output archive was not created");
        assertTrue(ZipUtils.isZipArchive(outputArchive.getAbsolutePath()),
                "The output file is not a valid zip archive");
    }

    @Test
    void testRunWhenDbFileDoesNotExist() {
        Path wrongDbPath = tempDir.resolve("non_existing.db");
        BackupCompiler backupCompiler =
                new BackupCompiler(wrongDbPath, outputPath, secrets, NOTESR_VERSION);

        assertThrows(BackupIOException.class, backupCompiler::run,
                "Expected BackupIOException when database file does not exist");
    }
}
