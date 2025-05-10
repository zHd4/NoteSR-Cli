package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockDao;
import app.notesr.cli.db.dao.FileInfoDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
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
import java.util.stream.Collectors;

import static app.notesr.cli.compiler.FileInfoJsonWriter.FILES_DATA_BLOCKS_ARRAY_NAME;
import static app.notesr.cli.compiler.FileInfoJsonWriter.FILES_INFOS_ARRAY_NAME;
import static app.notesr.cli.util.ModelGenerator.generateTestDataBlocks;
import static app.notesr.cli.util.ModelGenerator.generateTestFilesInfos;
import static app.notesr.cli.util.ModelGenerator.generateTestNote;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileInfoJsonWriterTest {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int TEST_FILES_COUNT = 5;
    private static final int TEST_BLOCK_SIZE = 1000;
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private JsonGenerator jsonGenerator;
    private File outputFile;

    private FileInfoDao fileInfoDao;
    private DataBlockDao dataBlockDao;

    private Set<FileInfo> testFilesInfos;
    private Set<DataBlock> testDataBlocks;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        outputFile = Path.of(getTempPath(randomUUID() + ".json").toString()).toFile();

        JsonFactory jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(outputFile, JsonEncoding.UTF8);

        fileInfoDao = mock(FileInfoDao.class);
        dataBlockDao = mock(DataBlockDao.class);

        testFilesInfos = generateTestFilesInfos(generateTestNote(), TEST_FILES_COUNT, MIN_FILE_SIZE, MAX_FILE_SIZE);
        testDataBlocks = testFilesInfos.stream()
                .flatMap(fileInfo -> generateTestDataBlocks(fileInfo, TEST_BLOCK_SIZE).stream())
                .peek(dataBlock -> dataBlock.setData(null))
                .collect(Collectors.toSet());

        when(fileInfoDao.getAll()).thenReturn(testFilesInfos);
        when(dataBlockDao.getAllDataBlocksWithoutData()).thenReturn(testDataBlocks);
    }

    @Test
    void testWrite() throws SQLException, IOException {
        FileInfoJsonWriter fileInfoJsonWriter = new FileInfoJsonWriter(jsonGenerator, fileInfoDao, dataBlockDao,
                DATETIME_FORMATTER);

        fileInfoJsonWriter.write();
        String outputFileJsonData = Files.readString(outputFile.toPath());

        Set<FileInfo> actualFilesInfos = deserializeResultFileData(outputFileJsonData, FILES_INFOS_ARRAY_NAME,
                new TypeReference<>() { });

        Set<DataBlock> actualDataBlocks = deserializeResultFileData(outputFileJsonData, FILES_DATA_BLOCKS_ARRAY_NAME,
                new TypeReference<>() { });

        assertEquals(testFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(testDataBlocks, actualDataBlocks, "Data blocks infos are different");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (outputFile.exists()) {
            boolean deleted = outputFile.delete();

            if (!deleted) {
                throw new IOException("Cannot delete temp file " + outputFile.getAbsolutePath());
            }
        }
    }

    private <T> Set<T> deserializeResultFileData(String json, String arrayName, TypeReference<Set<T>> typeReference)
            throws IOException {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATETIME_FORMATTER));

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonNode root = objectMapper.readTree(json);
        JsonNode array = root.get(arrayName);

        return objectMapper.readerFor(typeReference).readValue(array);
    }
}
