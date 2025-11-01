package app.notesr.cli.core.exception;

import lombok.Getter;

@Getter
public class NoteNotFoundException extends Exception {
    private final String noteId;

    public NoteNotFoundException(String noteId) {
        super("Note with id " + noteId + " not found");
        this.noteId = noteId;
    }
}
