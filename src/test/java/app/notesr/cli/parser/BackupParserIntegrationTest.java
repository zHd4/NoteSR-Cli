package app.notesr.cli.parser;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static app.notesr.cli.util.DbUtils.getTableData;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BackupParserIntegrationTest {
    private static final String BASE_FIXTURES_PATH = "parser/backup_parser";

    private static final String NOTES_FIXTURE_NAME = "expected-notes.json";
    private static final String FILES_INFOS_FIXTURE_NAME = "expected-files-infos.json";
    private static final String DATA_BLOCKS_FIXTURE_NAME = "expected-data-blocks.json";

    private static final String BACKUP_FIXTURE_NAME_PATTERN = "backup.";

    private static final String NOTES_TABLE_NAME = "notes";
    private static final String FILES_INFOS_TABLE_NAME = "files_info";
    private static final String DATA_BLOCKS_TABLE_NAME = "data_blocks";

    private Path parserTempDirPath;
    private Path dbPath;

    @BeforeEach
    public void beforeEach() {
        String uuid = randomUUID().toString();

        parserTempDirPath = Path.of(getTempPath(uuid) + "_temp");
        dbPath = Path.of(getTempPath(uuid) + ".db");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    public void testParser(String formatVersion) throws IOException, SQLException {
        Path backupPath = getBackupPath(formatVersion);
        BackupParser parser = new BackupParser(backupPath, dbPath);

        parser.setTempDirPath(parserTempDirPath);
        parser.run();

        DbConnection db = new DbConnection(dbPath.toString());

        String expectedNotesPath = getPathOfFixtureByName(NOTES_FIXTURE_NAME, formatVersion);
        String expectedFilesInfosPath = getPathOfFixtureByName(FILES_INFOS_FIXTURE_NAME, formatVersion);
        String expectedDataBlocksPath = getPathOfFixtureByName(DATA_BLOCKS_FIXTURE_NAME, formatVersion);

        List<Note> expectedNotes = getNotesFromMaps(parseJsonFixture(expectedNotesPath));
        List<Note> actualNotes = getNotesFromMaps(getTableData(db.getConnection(), NOTES_TABLE_NAME));

        List<FileInfo> expectedFilesInfos = getFilesInfosFromMaps(parseJsonFixture(expectedFilesInfosPath));
        List<FileInfo> actualFilesInfos =
                getFilesInfosFromMaps(getTableData(db.getConnection(), FILES_INFOS_TABLE_NAME));

        List<DataBlock> expectedDataBlocks = getDataBlocksFromMaps(parseJsonFixture(expectedDataBlocksPath));
        List<DataBlock> actualDataBlocks =
                getDataBlocksFromMaps(getTableData(db.getConnection(), DATA_BLOCKS_TABLE_NAME));

        assertEquals(expectedNotes, actualNotes, "Notes are different");
        assertEquals(expectedFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(expectedDataBlocks, actualDataBlocks, "Data blocks are different");
    }

    @AfterEach
    public void afterEach() throws IOException {
        if (dbPath.toFile().exists()) {
            Files.delete(dbPath);
        }

        if (parserTempDirPath.toFile().exists()) {
            deleteDir(parserTempDirPath);
        }
    }

    private static List<Note> getNotesFromMaps(List<Map<String, Object>> maps) {
        return maps.stream()
                .map(line -> Note.builder()
                        .id((String) line.get("id"))
                        .name((String) line.get("name"))
                        .text((String) line.get("text"))
                        .updatedAt(parseDateTime((String) line.get("updated_at")))
                        .build())
                .toList();
    }

    private static List<FileInfo> getFilesInfosFromMaps(List<Map<String, Object>> maps) {
        return maps.stream()
                .map(line -> FileInfo.builder()
                        .id((String) line.get("id"))
                        .noteId((String) line.get("note_id"))
                        .name((String) line.get("name"))
                        .size((Long) line.get("type"))
                        .createdAt(parseDateTime((String) line.get("created_at")))
                        .updatedAt(parseDateTime((String) line.get("updated_at")))
                        .build())
                .toList();
    }

    private static List<DataBlock> getDataBlocksFromMaps(List<Map<String, Object>> maps) {
        return maps.stream()
                .map(line -> DataBlock.builder()
                        .id((String) line.get("id"))
                        .fileId((String) line.get("file_id"))
                        .order(Long.valueOf((Integer) line.get("block_order")))
                        .data(parseDataBlockData(line.get("data")))
                        .build())
                .toList();
    }

    private static byte[] parseDataBlockData(Object data) {
        if (data instanceof String) {
            return Base64.getDecoder().decode(String.valueOf(data));
        } else if (data instanceof byte[]) {
            return (byte[]) data;
        } else {
            throw new IllegalArgumentException("Unexpected instance");
        }
    }

    private static List<Map<String, Object>> parseJsonFixture(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new String(readFixture(path)), new TypeReference<>() { });
    }

    private static String getPathOfFixtureByName(String name, String formatVersion) {
        return Path.of(BASE_FIXTURES_PATH, formatVersion, name).toString();
    }

    private static Path getBackupPath(String formatVersion) throws FileNotFoundException {
        File dir = Path.of(getFixturePath(BASE_FIXTURES_PATH).toString(), formatVersion).toFile();
        File[] results =
                requireNonNull(dir.listFiles((_, name) -> name.startsWith(BACKUP_FIXTURE_NAME_PATTERN)));

        if (results.length == 0) {
            throw new FileNotFoundException("Backup not found in " + dir.getAbsolutePath());
        }

        return results[0].toPath();
    }

    private static void deleteDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
