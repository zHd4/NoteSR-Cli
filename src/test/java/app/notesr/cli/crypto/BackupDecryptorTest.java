package app.notesr.cli.crypto;

import org.junit.jupiter.api.BeforeAll;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class BackupDecryptorTest {
    private static SecretKey key;
    private static byte[] salt;

    private static FileInputStream encryptedBackupInputStream;
    private static FileOutputStream tempDecryptedBackupOutputStream;

    private static Path tempDecryptedBackupFilePath;

    @BeforeAll
    public static void beforeAll() throws IOException {
        byte[] keyBytes = readFixture("aes256-key");

        key = new SecretKeySpec(keyBytes, 0, keyBytes.length, Aes.KEY_GENERATOR_ALGORITHM);
        salt = readFixture("aes256-salt");

        encryptedBackupInputStream = (FileInputStream)
                Files.newInputStream(generateFixturePath("encrypted.notesr.bak"));

        String tempPath = System.getProperty("java.io.tmpdir");

        tempDecryptedBackupFilePath = Path.of(tempPath, "test-decrypted.notesr.bak");
        tempDecryptedBackupOutputStream = (FileOutputStream) Files.newOutputStream(tempDecryptedBackupFilePath);
    }

    private static byte[] readFixture(String filename) throws IOException {
        return Files.readAllBytes(generateFixturePath(filename));
    }

    private static Path generateFixturePath(String filename) {
        return Path.of("src/test/resources/fixtures/" + filename);
    }
}