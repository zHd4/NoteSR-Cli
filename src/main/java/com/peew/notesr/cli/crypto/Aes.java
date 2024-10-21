package com.peew.notesr.cli.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Aes {
    public static final int KEY_SIZE = 256;
    public static final int SALT_SIZE = 16;

    public static final String KEY_GENERATOR_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKey key;
    private final byte[] salt;

    public Aes(SecretKey key, byte[] salt) {
        this.key = key;
        this.salt = salt;
    }

    public static Cipher createCipher(SecretKey key, byte[] iv, int mode) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), KEY_GENERATOR_ALGORITHM);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }

    public static SecretKey generateRandomKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(KEY_GENERATOR_ALGORITHM);
        generator.init(KEY_SIZE);

        return generator.generateKey();
    }

    public static byte[] generateRandomSalt() {
        byte[] result = new byte[SALT_SIZE];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(result);

        return result;
    }

    public byte[] encrypt(byte[] plainData) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        return createCipher(key, salt, Cipher.ENCRYPT_MODE).doFinal(plainData);
    }

    public byte[] decrypt(byte[] encryptedData) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        return createCipher(key, salt, Cipher.DECRYPT_MODE).doFinal(encryptedData);
    }
}
