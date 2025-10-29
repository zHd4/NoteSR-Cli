package app.notesr.cli.db;

public class ConnectionException extends RuntimeException {
    public ConnectionException(Throwable cause) {
        super(cause);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
