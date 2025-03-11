package app.notesr.cli.parser;

import app.notesr.cli.db.DbConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static app.notesr.cli.util.DbUtils.getTableData;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BackupParserIntegrationTest {
//    private static final String BACKUP_V1_FIXTURE_NAME = "backup-v1.json";
    private static final String BACKUP_V2_FIXTURE_NAME = "backup-v2.zip";

    private static final String NOTES_FIXTURE_PATH = "parser/backup_parser/expected-notes.json";
    private static final String FILES_INFOS_FIXTURE_PATH = "parser/backup_parser/expected-files-infos.json";
    private static final String DATA_BLOCKS_FIXTURE_PATH = "parser/backup_parser/expected-data-blocks.json";

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
    @ValueSource(strings = {/* BACKUP_V1_FIXTURE_NAME, */ BACKUP_V2_FIXTURE_NAME})
    public void testParser(String backupFixtureName) throws IOException, SQLException {
        Path backupPath = getFixturePath("parser/backup_parser/" + backupFixtureName);
        BackupParser parser = new BackupParser(backupPath, dbPath);

        parser.setTempDirPath(parserTempDirPath);
        parser.run();

        DbConnection db = new DbConnection(dbPath.toString());

        List<Map<String, Object>> expectedNotes = parseJsonFixture(NOTES_FIXTURE_PATH);
        List<Map<String, Object>> actualNotes = getTableData(db.getConnection(), NOTES_TABLE_NAME);

        List<Map<String, Object>> expectedFilesInfos = parseJsonFixture(FILES_INFOS_FIXTURE_PATH);
        List<Map<String, Object>> actualFilesInfos = getTableData(db.getConnection(), FILES_INFOS_TABLE_NAME);

        List<Map<String, Object>> expectedDataBlocks = parseJsonFixture(DATA_BLOCKS_FIXTURE_PATH);
        List<Map<String, Object>> actualDataBlocks = getTableData(db.getConnection(), DATA_BLOCKS_TABLE_NAME);

        assertEquals(expectedNotes, actualNotes, "Notes are different");
        assertEquals(expectedFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(expectedDataBlocks, actualDataBlocks, "Data blocks are different");
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteDir(parserTempDirPath);
        Files.delete(dbPath);
    }

    private static List<Map<String, Object>> parseJsonFixture(String path) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new String(readFixture(path)), new TypeReference<>() { });
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
}
