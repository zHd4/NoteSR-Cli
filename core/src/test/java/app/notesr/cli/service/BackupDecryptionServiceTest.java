package app.notesr.cli.service;

import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.dto.CryptoKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.CryptoKeyUtils.hexToCryptoKey;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupDecryptionServiceTest {
    private final BackupDecryptionService backupDecryptionService = new BackupDecryptionService();

    @ParameterizedTest
    @ValueSource(strings = {"encrypted-v1.notesr.bak", "encrypted-v2.notesr.bak"})
    void decryptWithValidFileAndKeyReturnsDecryptedFile(String encryptedFixtureName) throws Exception {
        File encryptedBackupFile = getFixturePath(encryptedFixtureName).toFile();
        Path keyPath = getFixturePath("crypto_key.txt");

        CryptoKey key = hexToCryptoKey(Files.readString(keyPath), KEY_GENERATOR_ALGORITHM);
        File decryptedBackupFile = backupDecryptionService.decrypt(encryptedBackupFile, key);

        assertTrue(decryptedBackupFile.exists(), "Decrypted file should exist");
        assertTrue(decryptedBackupFile.length() > 0, "Decrypted file should not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {"encrypted-v1.notesr.bak", "encrypted-v2.notesr.bak"})
    void decryptWithInvalidKeyThrowsFileDecryptionException(String encryptedFixtureName) {
        File encryptedBackupFile = getFixturePath(encryptedFixtureName).toFile();
        byte[] key = "invalid_key".getBytes();
        byte[] salt = "invalid_salt".getBytes();

        CryptoKey invalidKey = new CryptoKey(new SecretKeySpec(key, 0, key.length, KEY_GENERATOR_ALGORITHM),
                salt);

        assertThrows(FileDecryptionException.class, () ->
                backupDecryptionService.decrypt(encryptedBackupFile, invalidKey),
                "Decrypting with an invalid key should throw FileDecryptionException");
    }
}
