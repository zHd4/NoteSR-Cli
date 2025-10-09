package app.notesr.cli.exception;

import java.security.GeneralSecurityException;

public final class BackupEncryptionException extends RuntimeException {
    public BackupEncryptionException(GeneralSecurityException e) {
        super(e);
    }
}
