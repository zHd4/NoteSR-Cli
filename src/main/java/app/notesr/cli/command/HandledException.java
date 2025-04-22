package app.notesr.cli.command;

import lombok.Getter;

@Getter
class HandledException extends Exception {
    private final int exitCode;

    public HandledException(int exitCode) {
        this.exitCode = exitCode;
    }
}
