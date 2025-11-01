package app.notesr.cli.service.compiler;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupIOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.core.util.test.FixtureUtils.readFixture;
import static app.notesr.cli.core.util.KeyUtils.getKeyBytesFromHex;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BackupEncryptionServiceTest {
    @TempDir
    private Path tempDir;

    @Test
    void testEncrypt() throws Exception {
        File inputFile = Files.createFile(tempDir.resolve("input.zip")).toFile();
        File outputFile = tempDir.resolve("output.bak").toFile();
        Files.writeString(inputFile.toPath(), "test content");

        String keyHex = readFixture("shared/crypto_key.txt", tempDir);
        CryptoSecrets secrets = new CryptoSecrets(getKeyBytesFromHex(keyHex));
        BackupEncryptionService service = new BackupEncryptionService();

        service.encrypt(inputFile, outputFile, secrets);

        assertTrue(outputFile.exists(), "Encrypted output file should be created");
        assertTrue(outputFile.length() > 0,
            "Encrypted output file should not be empty");
    }

    @Test
    void testEncryptWhenShouldThrowIOException() {
        File inputFile = new File("nonexistent.zip");
        File outputFile = new File("should_not_exist.bak");
        CryptoSecrets badSecrets = mock(CryptoSecrets.class);

        BackupEncryptionService service = new BackupEncryptionService();
        assertThrows(BackupIOException.class, () ->
                        service.encrypt(inputFile, outputFile, badSecrets),
                "encrypt(...) should throw BackupIOException for non-existent input file");
    }
}
