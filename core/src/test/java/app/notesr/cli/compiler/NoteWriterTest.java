package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static app.notesr.cli.util.ModelGenerator.generateTestNotes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteWriterTest {
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TEST_NOTES_COUNT = 1; // single note is enough for these checks

    @Mock
    private NoteEntityDao noteEntityDao;

    @TempDir
    private Path tempDir;

    private File outputFile;
    private JsonGenerator jsonGenerator;

    @BeforeEach
    void setUp() throws IOException {
        outputFile = tempDir.resolve("test.json").toFile();

        JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputFile, JsonEncoding.UTF8);

        Set<Note> testNotes = generateTestNotes(TEST_NOTES_COUNT);
        when(noteEntityDao.getAll()).thenReturn(testNotes);
    }

    @Test
    void testWriteWithSupportedVersionIncludesCreatedAt() throws IOException {
        NoteWriter noteWriter = new NoteWriter(
                jsonGenerator,
                noteEntityDao,
                "5.2.0",   // minimum version where created_at must be included
                DATETIME_FORMATTER
        );

        noteWriter.write();
        String jsonOutput = Files.readString(outputFile.toPath());

        JsonNode noteNode = extractSingleNote(jsonOutput);
        assertTrue(noteNode.has("created_at"), "Field 'created_at' must be present");
        assertTrue(noteNode.has("updated_at"), "Field 'updated_at' must be present");
    }

    @Test
    void testWriteWithOlderVersionExcludesCreatedAt() throws IOException {
        NoteWriter noteWriter = new NoteWriter(
                jsonGenerator,
                noteEntityDao,
                "5.1.9",   // version below minimum where created_at must be included
                DATETIME_FORMATTER
        );

        noteWriter.write();
        String jsonOutput = Files.readString(outputFile.toPath());

        JsonNode noteNode = extractSingleNote(jsonOutput);
        assertFalse(noteNode.has("created_at"), "Field 'created_at' must not be present");
        assertTrue(noteNode.has("updated_at"), "Field 'updated_at' must be present");
    }

    private JsonNode extractSingleNote(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode root = objectMapper.readTree(json);
        JsonNode notesArray = root.get(NoteWriter.NOTES_ARRAY_NAME);

        assertEquals(1, notesArray.size(), "Exactly one note is expected in the test data");
        return notesArray.get(0);
    }
}
