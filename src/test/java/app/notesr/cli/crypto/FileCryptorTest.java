package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.FileDecryptionException;
import app.notesr.cli.crypto.exception.FileEncryptionException;
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

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileCryptorTest {
    private Path tempBackupPath;

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    public void testEncrypt(String backupFormatVersion) throws IOException, NoSuchAlgorithmException,
            FileEncryptionException {
        FileInputStream inputStream = new FileInputStream(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted").toString());

        tempBackupPath = PathUtils.getTempPath("test-encrypted-" + randomUUID());
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        FileCryptor decryptor = new FileCryptor(getCryptoKey());
        decryptor.encrypt(inputStream, outputStream);

        String expectedHash = computeSha256(getFixturePath(String.format("encrypted-%s.notesr.bak",
                backupFormatVersion)).toString());
        String actualHash = computeSha256(tempBackupPath.toString());

        assertEquals(expectedHash, actualHash, "Encrypted backup hash not matching with expected");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    public void testDecrypt(String backupFormatVersion) throws IOException, FileDecryptionException,
            NoSuchAlgorithmException {
        FileInputStream inputStream = new FileInputStream(
                getFixturePath(String.format("encrypted-%s.notesr.bak", backupFormatVersion)).toString());

        tempBackupPath = PathUtils.getTempPath("test-decrypted-" + randomUUID());
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        FileCryptor decryptor = new FileCryptor(getCryptoKey());
        decryptor.decrypt(inputStream, outputStream);

        String expectedHash = computeSha256(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted").toString());
        String actualHash = computeSha256(tempBackupPath.toString());

        assertEquals(expectedHash, actualHash, "Decrypted backup hash not matching with expected");
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (tempBackupPath != null && Files.exists(tempBackupPath)) {
            Files.delete(tempBackupPath);
        }
    }

    private CryptoKey getCryptoKey() throws IOException {
        String hexCryptoKey = readFixture("crypto_key.txt");
        return CryptoKeyUtils.hexToCryptoKey(hexCryptoKey, KEY_GENERATOR_ALGORITHM);
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
