package app.notesr.cli.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BackupDecryptionException extends Exception {
    public BackupDecryptionException(Exception e) {
        super(e);
    }

    public BackupDecryptionException(String message) {
        super(message);
    }
}
