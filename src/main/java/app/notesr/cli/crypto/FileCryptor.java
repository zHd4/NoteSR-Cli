package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.FileDecryptionException;
import app.notesr.cli.crypto.exception.FileEncryptionException;
import lombok.AllArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public final class FileCryptor {
    public static final String KEY_GENERATOR_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int CHUNK_SIZE = 100000;

    private CryptoKey cryptoKey;

    public void encrypt(FileInputStream inputStream, FileOutputStream outputStream) throws FileEncryptionException {
        try {
            Cipher cipher = createCipher(cryptoKey.getKey(), cryptoKey.getSalt(), Cipher.ENCRYPT_MODE);
            transformData(cipher, inputStream, outputStream);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | InvalidKeyException | IOException e) {
            throw new FileEncryptionException(e);
        }
    }

    public void decrypt(FileInputStream inputStream, FileOutputStream outputStream) throws FileDecryptionException {
        try {
            Cipher cipher = createCipher(cryptoKey.getKey(), cryptoKey.getSalt(), Cipher.DECRYPT_MODE);
            transformData(cipher, inputStream, outputStream);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | InvalidKeyException | IOException e) {
            throw new FileDecryptionException(e);
        }
    }

    private void transformData(Cipher cipher, FileInputStream inputStream, FileOutputStream outputStream)
            throws IOException {
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);

        try (cipherInputStream; outputStream) {
            byte[] chunk = new byte[CHUNK_SIZE];
            int bytesRead = cipherInputStream.read(chunk);

            while (bytesRead != -1) {
                if (bytesRead != CHUNK_SIZE) {
                    byte[] subChunk = new byte[bytesRead];
                    System.arraycopy(chunk, 0, subChunk, 0, bytesRead);

                    chunk = subChunk;
                }

                outputStream.write(chunk);

                chunk = new byte[CHUNK_SIZE];
                bytesRead = cipherInputStream.read(chunk);
            }
        }
    }

    private static Cipher createCipher(SecretKey key, byte[] iv, int mode) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), KEY_GENERATOR_ALGORITHM);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }
}
