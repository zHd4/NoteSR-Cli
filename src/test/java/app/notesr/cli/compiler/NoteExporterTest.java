package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteDao;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static app.notesr.cli.util.ModelGenerator.generateTestNotes;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoteExporterTest {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String NOTES_ARRAY_NAME = "notes";
    private static final int TEST_NOTES_COUNT = 5;

    private JsonGenerator jsonGenerator;
    private File outputFile;
    private NoteDao noteDao;
    private Set<Note> testNotes;

    @BeforeEach
    void beforeEach() throws SQLException, IOException {
        outputFile = Path.of(getTempPath(randomUUID() + ".json").toString()).toFile();

        JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputFile, JsonEncoding.UTF8);

        noteDao = mock(NoteDao.class);
        testNotes = generateTestNotes(TEST_NOTES_COUNT);

        when(noteDao.getAll()).thenReturn(testNotes);
    }

    @Test
    void testExport() throws SQLException, IOException {
        NoteExporter noteExporter = new NoteExporter(jsonGenerator, noteDao, DATETIME_FORMATTER);
        noteExporter.export();

        String outputFileJsonData = Files.readString(outputFile.toPath());
        Set<Note> actual = deserializeResultFileData(outputFileJsonData);

        assertEquals(testNotes, actual, "Notes are different");
    }

    @AfterEach
    void afterEach() throws IOException {
        if (outputFile.exists()) {
            boolean deleted = outputFile.delete();

            if (!deleted) {
                throw new IOException("Cannot delete temp file " + outputFile.getAbsolutePath());
            }
        }
    }

    private Set<Note> deserializeResultFileData(String json) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode root = objectMapper.readTree(json);
        JsonNode notesArray = root.get(NOTES_ARRAY_NAME);

        return objectMapper.readerFor(new TypeReference<Set<Note>>() { }).readValue(notesArray);
    }
}
