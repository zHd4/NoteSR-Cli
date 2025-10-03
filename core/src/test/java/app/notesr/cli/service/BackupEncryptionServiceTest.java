package app.notesr.cli.service;

import app.notesr.cli.dto.CryptoSecrets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.readFixture;
import static app.notesr.cli.util.KeyUtils.getKeyBytesFromHex;
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

        CryptoSecrets secrets = new CryptoSecrets(getKeyBytesFromHex(readFixture("crypto_key.txt", tempDir)));
        BackupEncryptionService service = new BackupEncryptionService();

        service.encrypt(inputFile, outputFile, secrets);

        assertTrue(outputFile.exists(), "Encrypted output file should be created");
        assertTrue(outputFile.length() > 0, "Encrypted output file should not be empty");
    }

    @Test
    void testEncryptWhenShouldThrowIOException() {
        File inputFile = new File("nonexistent.zip");
        File outputFile = new File("should_not_exist.bak");
        CryptoSecrets badSecrets = mock(CryptoSecrets.class);

        BackupEncryptionService service = new BackupEncryptionService();
        assertThrows(IOException.class, () ->
                        service.encrypt(inputFile, outputFile, badSecrets),
                "encrypt(...) should throw IOException for non-existent input file");
    }
}
