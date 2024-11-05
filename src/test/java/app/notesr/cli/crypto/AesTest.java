package app.notesr.cli.crypto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class AesTest {
    private static final int MIN_DATA_SIZE = 4096;
    private static final int MAX_DATA_SIZE = 10240;

    private static byte[] plainData;

    @BeforeAll
    public static void beforeAll() {
        Random random = new Random();
        int plainDataSize = random.nextInt((MAX_DATA_SIZE - MIN_DATA_SIZE) + 1) + MIN_DATA_SIZE;

        plainData = new byte[plainDataSize];
        random.nextBytes(plainData);
    }

    @Test
    public void testEncryptionAndDecryptionWithKey() throws
            NoSuchAlgorithmException, InvalidAlgorithmParameterException,
            NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        SecretKey key = Aes.generateRandomKey();
        byte[] salt = Aes.generateRandomSalt();

        Aes aesInstance = new Aes(key, salt);

        byte[] actualEncryptedData = aesInstance.encrypt(plainData);
        byte[] actualDecryptedData = aesInstance.decrypt(actualEncryptedData);

        assertArrayEquals(plainData, actualDecryptedData, "The original and decrypted data do not match");
    }
}
