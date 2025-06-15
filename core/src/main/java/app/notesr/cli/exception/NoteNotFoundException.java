package app.notesr.cli.exception;

public class NoteNotFoundException extends Exception {
    public NoteNotFoundException(String noteId) {
        super("Note with id " + noteId + " not found");
    }
}
