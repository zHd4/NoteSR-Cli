package app.notesr.cli.service.parser;

import app.notesr.cli.core.exception.BackupDecryptionException;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.service.parser.BackupDecryptionService;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

import static app.notesr.cli.core.util.test.FixtureUtils.getFixturePath;
import static app.notesr.cli.core.util.KeyUtils.getKeyBytesFromHex;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupDecryptionServiceTest {

    private static final int KEY_SIZE = 48;

    private final BackupDecryptionService backupDecryptionService = new BackupDecryptionService();

    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {
        "encrypted-v1.notesr.bak",
        "encrypted-v2.notesr.bak",
        "encrypted-v3.notesr.bak"})
    void decryptWithValidFileAndKeyReturnsDecryptedFile(String encryptedFixtureName)
        throws Exception {

        String encryptedBackupPathPart = Path.of("shared", encryptedFixtureName).toString();
        File encryptedBackupFile = getFixturePath(encryptedBackupPathPart, tempDir).toFile();

        Path keyPath = getFixturePath("shared/crypto_key.txt", tempDir);

        CryptoSecrets secrets = new CryptoSecrets(getKeyBytesFromHex(Files.readString(keyPath)));
        File decryptedBackupFile = backupDecryptionService.decrypt(encryptedBackupFile, secrets);

        assertTrue(decryptedBackupFile.exists(), "Decrypted file should exist");
        assertTrue(decryptedBackupFile.length() > 0,
            "Decrypted file should not be empty");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "encrypted-v1.notesr.bak",
        "encrypted-v2.notesr.bak",
        "encrypted-v3.notesr.bak"})
    void decryptWithInvalidKeyThrowsFileDecryptionException(String encryptedFixtureName)
        throws Exception {

        String encryptedBackupPathPart = Path.of("shared", encryptedFixtureName).toString();
        File encryptedBackupFile = getFixturePath(encryptedBackupPathPart, tempDir).toFile();

        byte[] invalidKeyBytes = new byte[KEY_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(invalidKeyBytes);

        CryptoSecrets secrets = new CryptoSecrets(invalidKeyBytes);

        assertThrows(BackupDecryptionException.class, () ->
                backupDecryptionService.decrypt(encryptedBackupFile, secrets),
                "Decrypting with an invalid key should throw BackupDecryptionException");
    }
}
