package app.notesr.cli.parser;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.mapper.DataBlocksJsonMapper;
import app.notesr.cli.util.mapper.FilesInfosJsonMapper;
import app.notesr.cli.util.mapper.NotesJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static app.notesr.cli.util.DbUtils.serializeTableAsJson;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackupParserTest {
    private static final String BASE_FIXTURES_PATH = "parser";

    private static final String NOTES_FIXTURE_NAME = "expected-notes.json";
    private static final String FILES_INFOS_FIXTURE_NAME = "expected-files-infos.json";
    private static final String DATA_BLOCKS_FIXTURE_NAME = "expected-data-blocks.json";

    private static final String BACKUP_FIXTURE_NAME_PATTERN = "backup.";

    private static final String NOTES_TABLE_NAME = "notes";
    private static final String FILES_INFOS_TABLE_NAME = "files_info";
    private static final String DATA_BLOCKS_TABLE_NAME = "data_blocks";

    @TempDir
    private Path tempDir;

    private Path parserTempDirPath;
    private Path dbPath;

    @BeforeEach
    void setUp() {
        parserTempDirPath = tempDir.resolve("parser_temp");
        dbPath = tempDir.resolve("test.db");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    void testParser(String formatVersion) throws IOException, SQLException {
        Path backupPath = getBackupPath(formatVersion);
        BackupParser parser = new BackupParser(backupPath, dbPath);

        parser.setTempDirPath(parserTempDirPath);
        parser.run();

        DbConnection db = new DbConnection(dbPath.toString());

        String expectedNotesPath = getPathOfFixtureByName(NOTES_FIXTURE_NAME, formatVersion);
        String expectedFilesInfosPath = getPathOfFixtureByName(FILES_INFOS_FIXTURE_NAME, formatVersion);
        String expectedDataBlocksPath = getPathOfFixtureByName(DATA_BLOCKS_FIXTURE_NAME, formatVersion);

        NotesJsonMapper notesMapper = new NotesJsonMapper();
        FilesInfosJsonMapper filesInfosMapper = new FilesInfosJsonMapper();
        DataBlocksJsonMapper dataBlocksMapper = new DataBlocksJsonMapper();

        List<Note> expectedNotes = notesMapper.map(readFixture(expectedNotesPath));
        List<Note> actualNotes = notesMapper.map(serializeTableAsJson(db.getConnection(), NOTES_TABLE_NAME));

        List<FileInfo> expectedFilesInfos = filesInfosMapper.map(readFixture(expectedFilesInfosPath));
        List<FileInfo> actualFilesInfos =
                filesInfosMapper.map(serializeTableAsJson(db.getConnection(), FILES_INFOS_TABLE_NAME));

        List<DataBlock> expectedDataBlocks = dataBlocksMapper.map(readFixture(expectedDataBlocksPath));
        List<DataBlock> actualDataBlocks =
                dataBlocksMapper.map(serializeTableAsJson(db.getConnection(), DATA_BLOCKS_TABLE_NAME));

        assertEquals(expectedNotes, actualNotes, "Notes are different");
        assertEquals(expectedFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(expectedDataBlocks, actualDataBlocks, "Data blocks are different");
    }

    private static String getPathOfFixtureByName(String name, String formatVersion) {
        return Path.of(BASE_FIXTURES_PATH, formatVersion, name).toString();
    }

    private static Path getBackupPath(String formatVersion) throws FileNotFoundException {
        File dir = Path.of(getFixturePath(BASE_FIXTURES_PATH).toString(), formatVersion).toFile();
        File[] results =
                requireNonNull(dir.listFiles((file, name) -> name.startsWith(BACKUP_FIXTURE_NAME_PATTERN)));

        if (results.length == 0) {
            throw new FileNotFoundException("Backup not found in " + dir.getAbsolutePath());
        }

        return results[0].toPath();
    }
}
