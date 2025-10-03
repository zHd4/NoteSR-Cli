package app.notesr.cli.service;

import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.dto.CryptoSecrets;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.KeyUtils.getKeyBytesFromHex;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupDecryptionServiceTest {
    private final BackupDecryptionService backupDecryptionService = new BackupDecryptionService();

    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"encrypted-v1.notesr.bak", "encrypted-v2.notesr.bak", "encrypted-v3.notesr.bak"})
    void decryptWithValidFileAndKeyReturnsDecryptedFile(String encryptedFixtureName) throws Exception {
        File encryptedBackupFile = getFixturePath(encryptedFixtureName, tempDir).toFile();
        Path keyPath = getFixturePath("crypto_key.txt", tempDir);

        CryptoSecrets secrets = new CryptoSecrets(getKeyBytesFromHex(Files.readString(keyPath)));
        File decryptedBackupFile = backupDecryptionService.decrypt(encryptedBackupFile, secrets);

        assertTrue(decryptedBackupFile.exists(), "Decrypted file should exist");
        assertTrue(decryptedBackupFile.length() > 0, "Decrypted file should not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"encrypted-v1.notesr.bak", "encrypted-v2.notesr.bak", "encrypted-v3.notesr.bak"})
    void decryptWithInvalidKeyThrowsFileDecryptionException(String encryptedFixtureName) {
        File encryptedBackupFile = getFixturePath(encryptedFixtureName, tempDir).toFile();

        byte[] invalidKeyBytes = "invalid_key".getBytes();
        CryptoSecrets secrets = new CryptoSecrets(invalidKeyBytes);

        assertThrows(FileDecryptionException.class, () ->
                backupDecryptionService.decrypt(encryptedBackupFile, secrets),
                "Decrypting with an invalid key should throw FileDecryptionException");
    }
}
