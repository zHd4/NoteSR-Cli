package app.notesr.cli.parser;

import app.notesr.cli.model.NoteInfo;

import java.io.InputStream;
import java.util.Set;

public final class NotesParser extends BaseParser {
    public NotesParser(InputStream stream) {
        super(stream);
    }

    @Override
    public void parse() {

    }

    public Set<NoteInfo> getNotesInfo() {
        throw new UnsupportedOperationException();
    }
}
