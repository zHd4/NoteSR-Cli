package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.VersionComparator;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
class NoteWriter implements Writer {
    static final String NOTES_ARRAY_NAME = "notes";
    private static final String MIN_NOTESR_VERSION_THAT_SUPPORTS_CREATED_AT = "5.2.0";

    private final JsonGenerator jsonGenerator;
    private final NoteEntityDao noteEntityDao;
    private final String noteSrVersion;
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

        if (new VersionComparator().compare(noteSrVersion, MIN_NOTESR_VERSION_THAT_SUPPORTS_CREATED_AT) >= 0) {
            String createdAt = note.getCreatedAt().format(dateTimeFormatter);
            jsonGenerator.writeStringField("created_at", createdAt);
        }

        String updatedAt = note.getUpdatedAt().format(dateTimeFormatter);
        jsonGenerator.writeStringField("updated_at", updatedAt);

        jsonGenerator.writeEndObject();
    }
}
