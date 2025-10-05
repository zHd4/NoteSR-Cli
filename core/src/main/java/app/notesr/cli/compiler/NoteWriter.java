package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
class NoteWriter implements Writer {
    static final String NOTES_ARRAY_NAME = "notes";

    private final JsonGenerator jsonGenerator;
    private final NoteEntityDao noteEntityDao;
    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void write() throws IOException {
        try (jsonGenerator) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeArrayFieldStart(NOTES_ARRAY_NAME);

            for (Note note : noteEntityDao.getAll()) {
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

        // TODO: write it depending on NoteSR version
        String createdAt = note.getCreatedAt().format(dateTimeFormatter);
        jsonGenerator.writeStringField("created_at", createdAt);

        String updatedAt = note.getUpdatedAt().format(dateTimeFormatter);
        jsonGenerator.writeStringField("updated_at", updatedAt);

        jsonGenerator.writeEndObject();
    }
}
