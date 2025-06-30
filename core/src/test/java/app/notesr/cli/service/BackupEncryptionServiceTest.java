package app.notesr.cli.service;

import app.notesr.cli.dto.CryptoKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.CryptoKeyUtils.hexToCryptoKey;
import static app.notesr.cli.util.FixtureUtils.readFixture;
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

        CryptoKey key = hexToCryptoKey(readFixture("crypto_key.txt"), KEY_GENERATOR_ALGORITHM);
        BackupEncryptionService service = new BackupEncryptionService();

        service.encrypt(inputFile, outputFile, key);

        assertTrue(outputFile.exists(), "Encrypted output file should be created");
        assertTrue(outputFile.length() > 0, "Encrypted output file should not be empty");
    }

    @Test
    void testEncryptWhenShouldThrowIOException() {
        File inputFile = new File("nonexistent.zip");
        File outputFile = new File("should_not_exist.bak");
        CryptoKey badKey = mock(CryptoKey.class);

        BackupEncryptionService service = new BackupEncryptionService();
        assertThrows(IOException.class, () ->
                service.encrypt(inputFile, outputFile, badKey),
                "encrypt(...) should throw IOException for non-existent input file");
    }
}
