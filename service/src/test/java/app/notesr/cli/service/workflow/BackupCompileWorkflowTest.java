package app.notesr.cli.service.workflow;

import app.notesr.cli.crypto.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static app.notesr.cli.util.test.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BackupCompileWorkflowTest {
    private static final String TEST_NOTESR_VERSION = "5.2.3";
    private static final int KEY_SIZE = 48;

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

        workflow.run(dbFile, tempArchive, outputBackup, secrets, TEST_NOTESR_VERSION);

        verify(compilationService, times(1))
                .compile(dbFile, tempArchive, secrets, TEST_NOTESR_VERSION);
        verify(encryptionService, times(1))
                .encrypt(tempArchive, outputBackup, secrets);
    }

    @Test
    void testRunWhenCompilationFails() throws Exception {
        File dbFile = tempDir.resolve("bad.db").toFile();
        File outputBackup = tempDir.resolve("output.bak").toFile();
        File tempArchive = tempDir.resolve("tmp-archive.zip").toFile();

        CryptoSecrets secrets = getTestCryptoSecrets();

        doThrow(new BackupIOException(""))
                .when(compilationService)
                .compile(dbFile, tempArchive, secrets, TEST_NOTESR_VERSION);

        assertThrows(BackupIOException.class, () ->
                        workflow.run(dbFile, tempArchive, outputBackup, secrets, TEST_NOTESR_VERSION),
                "run(...) should propagate BackupIOException if compilation fails");
    }

    private CryptoSecrets getTestCryptoSecrets() throws NoSuchAlgorithmException {
        byte[] keyBytes = new byte[KEY_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(keyBytes);

        return new CryptoSecrets(keyBytes);
    }
}
