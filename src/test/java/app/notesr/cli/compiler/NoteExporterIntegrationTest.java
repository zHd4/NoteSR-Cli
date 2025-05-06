package app.notesr.cli.compiler;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteDao;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import net.datafaker.Faker;
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
import java.util.List;

import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteExporterIntegrationTest {
    private static final String NOTES_TABLE_NAME = "notes";
    private static final String NOTES_ARRAY_NAME = "notes";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Faker FAKER = new Faker();
    private static final int TEST_NOTES_COUNT = 5;

    private DbConnection db;
    private JsonGenerator jsonGenerator;
    private NoteDao noteDao;
    private File outputFile;

    @BeforeEach
    public void beforeEach() throws SQLException, IOException {
        db = new DbConnection(":memory:");

        noteDao = new NoteDao(db);
        outputFile = Path.of(getTempPath(randomUUID() + ".json").toString()).toFile();
        jsonGenerator = getTestJsonGenerator(outputFile);

        fillNotesTable(noteDao);
    }

    @Test
    public void testExport() throws SQLException, IOException {
        NoteExporter noteExporter = new NoteExporter(jsonGenerator, noteDao, DATETIME_FORMATTER);
        noteExporter.export();

        String notesTableJsonData = DbUtils.serializeTableAsJson(db.getConnection(), NOTES_TABLE_NAME);
        String outputFileJsonData = Files.readString(outputFile.toPath());

        List<Note> expected = deserializeTableData(notesTableJsonData);
        List<Note> actual = deserializeResultFileData(outputFileJsonData);

        assertEquals(expected, actual, "Notes are different");
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (outputFile.exists()) {
            boolean deleted = outputFile.delete();

            if (!deleted) {
                throw new IOException("Cannot delete temp file " + outputFile.getAbsolutePath());
            }
        }
    }

    private List<Note> deserializeTableData(String json) throws JsonProcessingException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(javaTimeModule);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    private List<Note> deserializeResultFileData(String json) throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode root = objectMapper.readTree(json);
        JsonNode notesArray = root.get(NOTES_ARRAY_NAME);

        return objectMapper.readerFor(new TypeReference<List<Note>>() {}).readValue(notesArray);
    }

    private JsonGenerator getTestJsonGenerator(File outputFile) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        return jsonFactory.createGenerator(outputFile, JsonEncoding.UTF8);
    }

    private void fillNotesTable(NoteDao noteDao) throws SQLException {
        for (int i = 0; i < TEST_NOTES_COUNT; i++) {
            Note testNote = Note.builder()
                    .id(randomUUID().toString())
                    .name(FAKER.text().text(5, 15))
                    .text(FAKER.text().text())
                    .updatedAt(truncateDateTime(LocalDateTime.now()))
                    .build();

            noteDao.add(testNote);
        }
    }
}