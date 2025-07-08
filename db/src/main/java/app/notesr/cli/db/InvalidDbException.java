package app.notesr.cli.db;

public class InvalidDbException extends ConnectionException {
    public InvalidDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
