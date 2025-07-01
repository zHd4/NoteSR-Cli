package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.NoteEntityDao;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static app.notesr.cli.util.ModelGenerator.generateTestNotes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteWriterTest {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TEST_NOTES_COUNT = 5;

    @Mock
    private NoteEntityDao noteEntityDao;

    @TempDir
    private Path tempDir;

    private JsonGenerator jsonGenerator;
    private File outputFile;

    private Set<Note> testNotes;

    @BeforeEach
    void setUp() throws IOException {
        outputFile = tempDir.resolve("test.json").toFile();

        JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputFile, JsonEncoding.UTF8);

        testNotes = generateTestNotes(TEST_NOTES_COUNT);

        when(noteEntityDao.getAll()).thenReturn(testNotes);
    }

    @Test
    void testWrite() throws IOException {
        NoteWriter noteWriter = new NoteWriter(jsonGenerator, noteEntityDao, DATETIME_FORMATTER);
        noteWriter.write();

        String outputFileJsonData = Files.readString(outputFile.toPath());
        Set<Note> actual = deserializeResultFileData(outputFileJsonData);

        assertEquals(testNotes, actual, "Notes are different");
    }

    private Set<Note> deserializeResultFileData(String json) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode root = objectMapper.readTree(json);
        JsonNode notesArray = root.get(NoteWriter.NOTES_ARRAY_NAME);

        return objectMapper.readerFor(new TypeReference<Set<Note>>() { }).readValue(notesArray);
    }
}
