package app.notesr.cli.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.notesr.cli.security.crypto.dto.CryptoSecrets;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import javax.crypto.SecretKey;

class KeyUtilsTest {

    private static final int KEY_SIZE = 256;
    private static final String KEY_GENERATOR_ALGORITHM = "AES";

    @Test
    void testGetSecretKeyFromSecretsTruncatesOrPadsCorrectly() {
        int keyLength = KEY_SIZE / 8;
        byte[] longKey = new byte[40];

        for (int i = 0; i < longKey.length; i++) {
            longKey[i] = (byte) i;
        }

        CryptoSecrets secrets = new CryptoSecrets(longKey);
        SecretKey secretKey = KeyUtils.getSecretKeyFromSecrets(secrets);

        assertEquals(KEY_GENERATOR_ALGORITHM, secretKey.getAlgorithm());

        byte[] expected = Arrays.copyOfRange(longKey, 0, keyLength);
        assertArrayEquals(expected, secretKey.getEncoded());
    }

    @Test
    void testGetSecretKeyFromSecretsPadsWithZerosIfShorter() {
        int keyLength = KEY_SIZE / 8;

        byte[] shortKey = new byte[8];
        Arrays.fill(shortKey, (byte) 0x5A);

        CryptoSecrets secrets = new CryptoSecrets(shortKey);
        SecretKey secretKey = KeyUtils.getSecretKeyFromSecrets(secrets);

        byte[] expected = new byte[keyLength];
        Arrays.fill(expected, 0, shortKey.length, (byte) 0x5A);

        assertEquals(KEY_GENERATOR_ALGORITHM, secretKey.getAlgorithm());
        assertArrayEquals(expected, secretKey.getEncoded());
    }

    @Test
    void testGetKeyBytesFromHexParsesCorrectly() {
        String hex = "01 23 45 67\n89 AB CD EF";
        byte[] expected = new byte[] {
            0x01, 0x23, 0x45, 0x67,
            (byte) 0x89, (byte) 0xAB,
            (byte) 0xCD, (byte) 0xEF
        };

        byte[] actual = KeyUtils.getKeyBytesFromHex(hex);
        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetKeyBytesFromHexHandlesExtraWhitespace() {
        String hex = "  0a   1b  \t2c\n3d\r\n4e 5f ";

        byte[] expected = new byte[] {0x0a, 0x1b, 0x2c, 0x3d, 0x4e, 0x5f};
        byte[] actual = KeyUtils.getKeyBytesFromHex(hex);

        assertArrayEquals(expected, actual);
    }

    @Test
    void testGetKeyBytesFromHexThrowsOnInvalidHex() {
        String invalidHex = "zz yy xx";

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                KeyUtils.getKeyBytesFromHex(invalidHex));

        String message = exception.getMessage();

        assertNotNull(message);
        assertTrue(message.contains("Invalid hex key"));
    }

    @Test
    void testGetKeyBytesFromHexThrowsOnNullInput() {
        Exception exception = assertThrows(NullPointerException.class, () ->
                KeyUtils.getKeyBytesFromHex(null));

        String message = exception.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("hex must not be null"));
    }

    @Test
    void testGetSecretsFromHexProducesCorrectObject() {
        String hex = "0C 0D 0E";

        CryptoSecrets secrets = KeyUtils.getSecretsFromHex(hex);

        assertArrayEquals(new byte[] {0x0C, 0x0D, 0x0E}, secrets.getKey());
    }


    @Test
    void testGetIvExtractsIvCorrectly() {
        int keyLength = 48;
        int ivLength = 16;

        byte[] key = new byte[keyLength];

        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) i;
        }

        CryptoSecrets secrets = new CryptoSecrets(key);

        byte[] iv = KeyUtils.getIvFromSecrets(secrets);

        assertEquals(ivLength, iv.length);
        assertArrayEquals(Arrays.copyOfRange(key, keyLength - ivLength, keyLength), iv);
    }
}
