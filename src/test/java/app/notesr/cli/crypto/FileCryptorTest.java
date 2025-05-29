package app.notesr.cli.crypto;

import app.notesr.cli.dto.CryptoKey;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileCryptorTest {
    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void testEncrypt(String backupFormatVersion) throws IOException, NoSuchAlgorithmException,
            FileEncryptionException {
        FileInputStream inputStream = new FileInputStream(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted").toString());

        Path tempBackupPath = tempDir.resolve("encrypted-test-file");
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
    void testDecrypt(String backupFormatVersion) throws IOException, FileDecryptionException,
            NoSuchAlgorithmException {
        FileInputStream inputStream = new FileInputStream(
                getFixturePath(String.format("encrypted-%s.notesr.bak", backupFormatVersion)).toString());

        Path tempBackupPath = tempDir.resolve("decrypted-test-file");
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        FileCryptor decryptor = new FileCryptor(getCryptoKey());
        decryptor.decrypt(inputStream, outputStream);

        String expectedHash = computeSha256(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted").toString());
        String actualHash = computeSha256(tempBackupPath.toString());

        assertEquals(expectedHash, actualHash, "Decrypted backup hash not matching with expected");
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
