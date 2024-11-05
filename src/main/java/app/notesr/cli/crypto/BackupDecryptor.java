package app.notesr.cli.crypto;

import app.notesr.cli.crypto.exception.BackupDecryptionException;
import lombok.AllArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public final class BackupDecryptor {
    private static final int CHUNK_SIZE = 100000;

    private SecretKey key;
    private byte[] salt;

    public void decrypt(FileInputStream inputStream, FileOutputStream outputStream) throws BackupDecryptionException {
        try {
            Cipher cipher = Aes.createCipher(key, salt, Cipher.DECRYPT_MODE);
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

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | InvalidKeyException | IOException e) {
            throw new BackupDecryptionException(e);
        }
    }
}
