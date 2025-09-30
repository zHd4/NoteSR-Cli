package app.notesr.cli.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public interface AesCryptor {

    void encrypt(InputStream in, OutputStream out)
            throws GeneralSecurityException, IOException;

    void decrypt(InputStream in, OutputStream out)
            throws GeneralSecurityException, IOException;
}
