package app.notesr.cli.crypto;

import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.util.CryptoKeyUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static app.notesr.cli.util.HashUtils.computeSha512;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileCryptorTest {
    @TempDir
    private Path tempDir;

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void testEncrypt(String backupFormatVersion) throws IOException, NoSuchAlgorithmException,
            FileEncryptionException {
        FileInputStream inputStream = new FileInputStream(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted", tempDir).toString());

        Path tempBackupPath = tempDir.resolve("encrypted-test-file");
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        FileCryptor decryptor = new FileCryptor(getCryptoKey());
        decryptor.encrypt(inputStream, outputStream);

        String expectedHash = computeSha512(getFixturePath(String.format("encrypted-%s.notesr.bak",
                backupFormatVersion), tempDir).toString());
        String actualHash = computeSha512(tempBackupPath.toString());

        assertEquals(expectedHash, actualHash, "Encrypted backup hash not matching with expected");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void testDecrypt(String backupFormatVersion) throws IOException, FileDecryptionException,
            NoSuchAlgorithmException {
        FileInputStream inputStream = new FileInputStream(
                getFixturePath(String.format("encrypted-%s.notesr.bak", backupFormatVersion), tempDir).toString());

        Path tempBackupPath = tempDir.resolve("decrypted-test-file");
        FileOutputStream outputStream = new FileOutputStream(tempBackupPath.toString());

        FileCryptor decryptor = new FileCryptor(getCryptoKey());
        decryptor.decrypt(inputStream, outputStream);

        String expectedHash = computeSha512(getFixturePath(backupFormatVersion
                + ".notesr.bak.decrypted", tempDir).toString());
        String actualHash = computeSha512(tempBackupPath.toString());

        assertEquals(expectedHash, actualHash, "Decrypted backup hash not matching with expected");
    }

    private CryptoKey getCryptoKey() throws IOException {
        String hexCryptoKey = readFixture("crypto_key.txt", tempDir);
        return CryptoKeyUtils.hexToCryptoKey(hexCryptoKey, KEY_GENERATOR_ALGORITHM);
    }
}
