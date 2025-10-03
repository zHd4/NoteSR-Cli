package app.notesr.cli.service.workflow;

import app.notesr.cli.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static app.notesr.cli.util.KeyUtils.getKeyBytesFromHex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupCompileWorkflowTest {
    private static final String TEST_NOTESR_VERSION = "5.2.1";

    @Mock
    private BackupCompilationService compilationService;

    @Mock
    private BackupEncryptionService encryptionService;

    @TempDir
    private Path tempDir;

    private BackupCompileWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new BackupCompileWorkflow(compilationService, encryptionService);
    }

    @Test
    void testRun() throws Exception {
        File dbFile = getFixturePath("backup.db", tempDir).toFile();
        File outputBackup = tempDir.resolve("output.bak").toFile();
        File tempArchive = tempDir.resolve("tmp-archive.zip").toFile();

        CryptoSecrets secrets = getTestCryptoSecrets();
        Path tempCompiledDir = Files.createTempDirectory(tempDir, "tmp-compiled");

        when(compilationService.compile(dbFile, tempArchive, TEST_NOTESR_VERSION)).thenReturn(tempCompiledDir);
        Path actual = workflow.run(dbFile, tempArchive, outputBackup, secrets, TEST_NOTESR_VERSION);

        verify(compilationService, times(1)).compile(dbFile, tempArchive, TEST_NOTESR_VERSION);
        verify(encryptionService, times(1)).encrypt(tempArchive, outputBackup, secrets);

        assertEquals(tempCompiledDir, actual,
                "run(...) should return the same temp dir as compilation service");
    }

    @Test
    void testRunWhenShouldThrowBackupDbException() throws Exception {
        File dbFile = tempDir.resolve("bad.db").toFile();
        File outputBackup = tempDir.resolve("output.bak").toFile();
        File tempArchive = tempDir.resolve("tmp-archive.zip").toFile();

        CryptoSecrets secrets = getTestCryptoSecrets();

        when(compilationService.compile(dbFile, tempArchive, TEST_NOTESR_VERSION))
                .thenThrow(new BackupDbException(new Exception()));

        assertThrows(BackupDbException.class, () ->
                workflow.run(dbFile, tempArchive, outputBackup, secrets, TEST_NOTESR_VERSION),
                "run(...) should throw BackupDbException if compilation fails");
    }

    private CryptoSecrets getTestCryptoSecrets() throws IOException {
        return new CryptoSecrets(getKeyBytesFromHex(readFixture("crypto_key.txt", tempDir)));
    }
}
