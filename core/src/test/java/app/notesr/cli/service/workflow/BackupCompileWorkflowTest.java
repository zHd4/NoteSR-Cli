package app.notesr.cli.service.workflow;

import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.CryptoKeyUtils.hexToCryptoKey;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupCompileWorkflowTest {
    private static final String TEST_NOTESR_VERSION = "5.1";

    private BackupCompilationService compilationService;
    private BackupEncryptionService encryptionService;
    private BackupCompileWorkflow workflow;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        compilationService = mock(BackupCompilationService.class);
        encryptionService = mock(BackupEncryptionService.class);
        workflow = new BackupCompileWorkflow(compilationService, encryptionService);
    }

    @Test
    void testRun() throws Exception {
        File dbFile = getFixturePath("backup.db", tempDir).toFile();
        File outputBackup = tempDir.resolve("output.bak").toFile();
        File tempArchive = tempDir.resolve("tmp-archive.zip").toFile();

        CryptoKey cryptoKey = getTestCryptoKey();
        Path tempCompiledDir = Files.createTempDirectory(tempDir, "tmp-compiled");

        when(compilationService.compile(dbFile, tempArchive, TEST_NOTESR_VERSION)).thenReturn(tempCompiledDir);
        Path actual = workflow.run(dbFile, tempArchive, outputBackup, cryptoKey, TEST_NOTESR_VERSION);

        verify(compilationService, times(1)).compile(dbFile, tempArchive, TEST_NOTESR_VERSION);
        verify(encryptionService, times(1)).encrypt(tempArchive, outputBackup, cryptoKey);

        assertEquals(tempCompiledDir, actual,
                "run(...) should return the same temp dir as compilation service");
    }

    @Test
    void testRunWhenShouldThrowBackupDbException() throws Exception {
        File dbFile = tempDir.resolve("bad.db").toFile();
        File outputBackup = tempDir.resolve("output.bak").toFile();
        File tempArchive = tempDir.resolve("tmp-archive.zip").toFile();

        CryptoKey cryptoKey = getTestCryptoKey();

        when(compilationService.compile(dbFile, tempArchive, TEST_NOTESR_VERSION))
                .thenThrow(new BackupDbException(new Exception()));

        assertThrows(BackupDbException.class, () ->
                workflow.run(dbFile, tempArchive, outputBackup, cryptoKey, TEST_NOTESR_VERSION),
                "run(...) should throw BackupDbException if compilation fails");
    }

    private CryptoKey getTestCryptoKey() throws IOException {
        return hexToCryptoKey(readFixture("crypto_key.txt", tempDir), KEY_GENERATOR_ALGORITHM);
    }
}
