package app.notesr.cli.security.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.notesr.cli.security.crypto.AesGcmCryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

class AesGcmCryptorTest {

    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final String KEY_GENERATOR_ALGORITHM = "AES";
    private static final byte[] DATA = "GCM test string".getBytes();

    private static final SecureRandom RANDOM = new SecureRandom();

    private AesGcmCryptor cryptor;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        RANDOM.nextBytes(keyBytes);

        SecretKey key = new SecretKeySpec(keyBytes, KEY_GENERATOR_ALGORITHM);
        cryptor = new AesGcmCryptor(key);
    }

    @Test
    void testEncryptAndDecryptBytesReturnsOriginalData() throws Exception {
        byte[] encrypted = cryptor.encrypt(DATA);
        byte[] decrypted = cryptor.decrypt(encrypted);

        assertArrayEquals(DATA, decrypted, "Decrypted data must match original");
    }

    @Test
    void testEncryptAndDecryptStreamsReturnsOriginalData() throws Exception {
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        ByteArrayInputStream dataIn = new ByteArrayInputStream(DATA);

        cryptor.encrypt(dataIn, encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        cryptor.decrypt(encryptedIn, decryptedOut);

        assertArrayEquals(DATA, decryptedOut.toByteArray(),
                "Stream decrypted data must match original");
    }

    @Test
    void testEncryptWritesIvToOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(DATA);

        cryptor.encrypt(in, out);
        byte[] written = out.toByteArray();
        assertTrue(written.length > IV_SIZE,
                "Output must contain IV at the beginning");

        byte[] iv = Arrays.copyOfRange(written, 0, IV_SIZE);
        assertEquals(IV_SIZE, iv.length, "IV length must match IV_SIZE constant");
    }

    @Test
    void testDecryptThrowsExceptionOnTooShortInput() {
        byte[] badData = new byte[IV_SIZE - 1];
        ByteArrayInputStream in = new ByteArrayInputStream(badData);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assertThrows(IOException.class, () -> cryptor.decrypt(in, out));
    }
}
