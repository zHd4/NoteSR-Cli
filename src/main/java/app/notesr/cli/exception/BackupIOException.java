package app.notesr.cli.exception;

public class BackupIOException extends RuntimeException {
    public BackupIOException(String message) {
        super(message);
    }

    public BackupIOException(Throwable cause) {
        super(cause);
    }
}
