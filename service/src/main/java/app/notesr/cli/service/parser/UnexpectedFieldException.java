package app.notesr.cli.service.parser;

public class UnexpectedFieldException extends RuntimeException {
    public UnexpectedFieldException(String message) {
        super(message);
    }
}
