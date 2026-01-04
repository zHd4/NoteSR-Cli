/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.security.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;

class AesCbcCryptorTest {

    private static final int CHUNK_SIZE = 100_000;
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private static final String KEY_GENERATOR_ALGORITHM = "AES";
    private static final byte[] DATA = "CBC test data".getBytes();

    private static final SecureRandom RANDOM = new SecureRandom();

    private AesCbcCryptor cryptor;

    @BeforeEach
    void setUp() {
        byte[] keyBytes = new byte[KEY_SIZE / 8];
        RANDOM.nextBytes(keyBytes);

        byte[] iv = new byte[IV_SIZE];
        RANDOM.nextBytes(iv);

        SecretKey key = new SecretKeySpec(keyBytes, KEY_GENERATOR_ALGORITHM);
        cryptor = new AesCbcCryptor(key, iv);
    }

    @Test
    void testEncryptAndDecryptBytesReturnsOriginalData() throws Exception {
        byte[] encrypted = cryptor.encrypt(DATA);
        byte[] decrypted = cryptor.decrypt(encrypted);

        assertArrayEquals(DATA, decrypted, "Decrypted bytes must match original");
    }

    @Test
    void testEncryptAndDecryptStreamsReturnsOriginalData() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(DATA);
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();

        cryptor.encrypt(in, encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        cryptor.decrypt(encryptedIn, decryptedOut);

        assertArrayEquals(DATA, decryptedOut.toByteArray(),
                "Stream decrypted data must match original");
    }

    @Test
    void testEncryptAndDecryptLargeDataHandlesChunks() throws Exception {
        byte[] largeData = new byte[CHUNK_SIZE * 3 + 12345];
        new SecureRandom().nextBytes(largeData);

        ByteArrayInputStream in = new ByteArrayInputStream(largeData);
        ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();

        cryptor.encrypt(in, encryptedOut);

        ByteArrayInputStream encryptedIn = new ByteArrayInputStream(encryptedOut.toByteArray());
        ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream();

        cryptor.decrypt(encryptedIn, decryptedOut);

        assertArrayEquals(largeData, decryptedOut.toByteArray(),
                "Chunked stream decryption must match original");
    }
}
