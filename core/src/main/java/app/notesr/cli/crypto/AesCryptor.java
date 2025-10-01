package app.notesr.cli.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public interface AesCryptor {

    byte[] encrypt(byte[] plainData) throws GeneralSecurityException;

    byte[] decrypt(byte[] encryptedData) throws GeneralSecurityException;

    void encrypt(InputStream in, OutputStream out)
            throws GeneralSecurityException, IOException;

    void decrypt(InputStream in, OutputStream out)
            throws GeneralSecurityException, IOException;
}
