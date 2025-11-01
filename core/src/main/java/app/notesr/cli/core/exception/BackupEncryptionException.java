package app.notesr.cli.core.exception;

import java.security.GeneralSecurityException;

public final class BackupEncryptionException extends RuntimeException {
    public BackupEncryptionException(GeneralSecurityException e) {
        super(e);
    }
}
