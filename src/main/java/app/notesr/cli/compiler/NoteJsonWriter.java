package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteDao;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
class NoteJsonWriter implements JsonWriter {
    static final String NOTES_ARRAY_NAME = "notes";

    private final JsonGenerator jsonGenerator;
    private final NoteDao noteDao;
    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void write() throws IOException, SQLException {
        try (jsonGenerator) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeArrayFieldStart(NOTES_ARRAY_NAME);

            for (Note note : noteDao.getAll()) {
                writeNote(note);
            }

            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
    }

    private void writeNote(Note note) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", note.getId());

        jsonGenerator.writeStringField("name", note.getName());
        jsonGenerator.writeStringField("text", note.getText());

        String updatedAt = note.getUpdatedAt().format(dateTimeFormatter);
        jsonGenerator.writeStringField("updated_at", updatedAt);

        jsonGenerator.writeEndObject();
    }
}
