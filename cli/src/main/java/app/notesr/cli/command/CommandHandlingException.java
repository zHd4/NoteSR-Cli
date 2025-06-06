package app.notesr.cli.command;

import lombok.Getter;

@Getter
public class CommandHandlingException extends Exception {
    private final int exitCode;

    CommandHandlingException(int exitCode) {
        this.exitCode = exitCode;
    }
}
