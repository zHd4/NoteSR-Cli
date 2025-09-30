package app.notesr.cli.exception;

import java.io.IOException;

public class BackupIOException extends RuntimeException {
    public BackupIOException(String message) {
        super(message);
    }

    public BackupIOException(IOException cause) {
        super(cause);
    }
}
