package app.notesr.cli.exception;

import java.io.IOException;

public class BackupIOException extends RuntimeException {
    public BackupIOException(String message) {
        super(message);
    }

    public BackupIOException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}
