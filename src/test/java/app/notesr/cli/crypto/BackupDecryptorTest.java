package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.BackupDecryptionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackupDecryptorTest {
    private static final String DECRYPTED_BACKUP_HASH =
            "b7c8a729d50b341abdedcc731a409b5dd46456b2719889ff9cf004abbb8054cf";

    private static SecretKey key;
    private static byte[] salt;

    private static FileInputStream encryptedBackupInputStream;
    private static FileOutputStream tempDecryptedBackupOutputStream;
    private static Path tempDecryptedBackupFilePath;

    @BeforeEach
    public void beforeEach() throws IOException {
        byte[] keyBytes = readFixture("aes256-key");

        key = new SecretKeySpec(keyBytes, 0, keyBytes.length, Aes.KEY_GENERATOR_ALGORITHM);
        salt = readFixture("aes256-salt");
        encryptedBackupInputStream = new FileInputStream(generateFixturePath("encrypted.notesr.bak").toString());

        String tempPath = System.getProperty("java.io.tmpdir");

        tempDecryptedBackupFilePath = Path.of(tempPath, "test-decrypted.json");
        tempDecryptedBackupOutputStream = new FileOutputStream(tempDecryptedBackupFilePath.toString());
    }

    @Test
    public void testDecrypt() throws BackupDecryptionException, NoSuchAlgorithmException, IOException {
        BackupDecryptor decryptor = new BackupDecryptor(key, salt);
        decryptor.decrypt(encryptedBackupInputStream, tempDecryptedBackupOutputStream);

        String actualHash = sha256OfFile(tempDecryptedBackupFilePath.toString());

        assertEquals(DECRYPTED_BACKUP_HASH, actualHash);
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (Files.exists(tempDecryptedBackupFilePath)) {
            Files.delete(tempDecryptedBackupFilePath);
        }
    }

    private static byte[] readFixture(String filename) throws IOException {
        return Files.readAllBytes(generateFixturePath(filename));
    }

    private static Path generateFixturePath(String filename) {
        return Path.of("src/test/resources/fixtures", filename);
    }

    private static String sha256OfFile(String path) throws NoSuchAlgorithmException, IOException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        try (FileInputStream stream = new FileInputStream(path)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = stream.read(buffer)) != -1) {
                sha256.update(buffer, 0, bytesRead);
            }
        }

        StringBuilder hexString = new StringBuilder();

        for (byte b : sha256.digest()) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }
}
