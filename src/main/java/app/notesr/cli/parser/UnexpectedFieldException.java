package app.notesr.cli.parser;

public class UnexpectedFieldException extends RuntimeException {
    public UnexpectedFieldException(String message) {
        super(message);
    }
}
