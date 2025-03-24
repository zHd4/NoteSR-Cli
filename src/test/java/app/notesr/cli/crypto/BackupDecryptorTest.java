package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.BackupDecryptionException;
import app.notesr.cli.util.PathUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackupDecryptorTest {
    private Path tempBackupPath;

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    public void testDecrypt(String formatVersion) throws IOException, BackupDecryptionException,
            NoSuchAlgorithmException {
        String hexCryptoKey = readFixture("crypto_key.txt");
        CryptoKey cryptoKey = CryptoKeyUtils.hexToCryptoKey(hexCryptoKey, Aes.KEY_GENERATOR_ALGORITHM);

        FileInputStream inputStream = new FileInputStream(
                getFixturePath(String.format("encrypted-%s.notesr.bak", formatVersion)).toString());

        tempBackupPath = PathUtils.getTempPath("test-decrypted-" + randomUUID());
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        BackupDecryptor decryptor = new BackupDecryptor(cryptoKey);
        decryptor.decrypt(inputStream, outputStream);

        String expectedHash = readFixture("decrypted-" + formatVersion + ".sha256").trim();

        String actualHash = computeSha256(tempBackupPath.toString());
        assertEquals(expectedHash, actualHash, "Decrypted backup hash not matching with expected");
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (tempBackupPath != null && Files.exists(tempBackupPath)) {
            Files.delete(tempBackupPath);
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
