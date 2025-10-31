package app.notesr.cli.parser;

public class BackupParserException extends RuntimeException {
    public BackupParserException(String message) {
        super(message);
    }

    public BackupParserException(String message, Throwable e) {
        super(message, e);
    }
}
