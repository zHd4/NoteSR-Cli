package app.notesr.cli.parser;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class NotesJsonParser extends BaseJsonParser {
    private static final String ROOT_NAME = "notes";

    private final NoteEntityDao noteEntityDao;

    public NotesJsonParser(DbConnection db, JsonParser parser, DateTimeFormatter timestampFormatter) {
        super(parser, timestampFormatter);
        this.noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
    }

    @Override
    public void transferToDb() throws IOException {
        String field;

        if (!skipTo(ROOT_NAME)) {
            throw new BackupParserException("'" + ROOT_NAME + "' field not found in json");
        }

        do {
            Note note = new Note();

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                field = parser.getCurrentName();

                if (field != null) {
                    parseNote(note, field);
                }
            }

            noteEntityDao.add(note);
        } while (parser.nextToken() != JsonToken.END_ARRAY);
    }

    private void parseNote(Note note, String field) throws IOException {
        switch (field) {
            case "id" -> {
                if (parser.getValueAsString().equals("id")) {
                    return;
                }

                note.setId(parser.getValueAsString());
            }

            case "name" -> {
                if (parser.getValueAsString().equals("name")) {
                    return;
                }

                note.setName(parser.getValueAsString());
            }

            case "text" -> {
                if (parser.getValueAsString().equals("text")) {
                    return;
                }
                note.setText(parser.getValueAsString());
            }

            case "updated_at" -> {
                if (parser.getValueAsString().equals("updated_at")) {
                    return;
                }

                LocalDateTime updatedAt = LocalDateTime.parse(
                        parser.getValueAsString(),
                        getTimestampFormatter()
                );

                note.setUpdatedAt(updatedAt);
            }

            default -> {
                if (!field.equals(ROOT_NAME)) {
                    throw new UnexpectedFieldException("Unexpected field: " + field);
                }
            }
        }
    }
}
