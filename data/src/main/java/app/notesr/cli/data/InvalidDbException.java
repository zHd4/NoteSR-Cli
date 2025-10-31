package app.notesr.cli.data;

public class InvalidDbException extends ConnectionException {
    public InvalidDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
