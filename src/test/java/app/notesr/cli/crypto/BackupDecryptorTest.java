package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.BackupDecryptionException;
import app.notesr.cli.util.PathUtils;
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

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackupDecryptorTest {
    private static final String DECRYPTED_BACKUP_HASH =
            "b7c8a729d50b341abdedcc731a409b5dd46456b2719889ff9cf004abbb8054cf";

    @Test
    public void testDecrypt() throws BackupDecryptionException, NoSuchAlgorithmException, IOException {
        byte[] keyBytes = readFixture("crypto/backup_decryptor/aes256-key");

        SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, Aes.KEY_GENERATOR_ALGORITHM);
        byte[] salt = readFixture("crypto/backup_decryptor/aes256-salt");

        FileInputStream encryptedBackupInputStream = new FileInputStream(
                getFixturePath("crypto/backup_decryptor/encrypted.notesr.bak").toString());

        Path tempDecryptedBackupFilePath = Path.of(PathUtils.getTempPath("test-decrypted.json"));
        FileOutputStream tempDecryptedBackupOutputStream = new FileOutputStream(tempDecryptedBackupFilePath.toString());

        BackupDecryptor decryptor = new BackupDecryptor(key, salt);
        decryptor.decrypt(encryptedBackupInputStream, tempDecryptedBackupOutputStream);

        String actualHash = computeSha256(tempDecryptedBackupFilePath.toString());
        assertEquals(DECRYPTED_BACKUP_HASH, actualHash, "Decrypted backup hash not matching with expected");

        if (Files.exists(tempDecryptedBackupFilePath)) {
            Files.delete(tempDecryptedBackupFilePath);
        }
    }

    private static String computeSha256(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

        try (FileInputStream stream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = stream.read(buffer)) != -1) {
                sha256.update(buffer, 0, bytesRead);
            }
        }

        StringBuilder hexDigestBuilder = new StringBuilder();

        for (byte b : sha256.digest()) {
            hexDigestBuilder.append(String.format("%02x", b));
        }

        return hexDigestBuilder.toString();
    }
}
