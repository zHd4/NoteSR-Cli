package app.notesr.cli.service.parser;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.data.model.DataBlock;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import app.notesr.cli.data.mapper.DataBlocksJsonMapper;
import app.notesr.cli.data.mapper.FilesInfosJsonMapper;
import app.notesr.cli.data.mapper.NotesJsonMapper;
import app.notesr.cli.core.util.test.DbUtils;
import app.notesr.cli.service.parser.BackupParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static app.notesr.cli.core.util.test.FixtureUtils.getFixturePath;
import static app.notesr.cli.core.util.test.FixtureUtils.readFixture;
import static app.notesr.cli.core.util.KeyUtils.getKeyBytesFromHex;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BackupParserTest {
    private static final String NOTES_TABLE_NAME = "notes";
    private static final String FILES_INFOS_TABLE_NAME = "files_info";
    private static final String DATA_BLOCKS_TABLE_NAME = "data_blocks";

    private static final String NOTES_FIXTURE_PATH_FORMAT = "shared/parser/%s/expected-notes.json";

    private static final String FILES_INFOS_FIXTURE_PATH_FORMAT =
        "shared/parser/%s/expected-files-infos.json";

    private static final String DATA_BLOCKS_FIXTURE_PATH_FORMAT =
        "shared/parser/%s/expected-data-blocks.json";

    private static final String BACKUP_FIXTURE_PATH_FORMAT = "shared/parser/%s/backup.%s";

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
    @ValueSource(strings = {"v1.json", "v2.zip", "v3.zip"})
    void testParser(String format) throws IOException {
        String[] parsedFormat = format.split("\\.");

        String formatVersion = parsedFormat[0];
        String backupExtension = parsedFormat[1];

        Path backupPath = getFixturePath(
            String.format(BACKUP_FIXTURE_PATH_FORMAT, formatVersion, backupExtension), tempDir);

        CryptoSecrets secrets = getTestSecrets();

        BackupParser parser = new BackupParser(backupPath, dbPath, secrets);

        parser.setTempDirPath(parserTempDirPath);
        parser.run();

        DbConnection db = new DbConnection(dbPath.toString());

        String notesPath = String.format(NOTES_FIXTURE_PATH_FORMAT, formatVersion);
        String filesInfosPath = String.format(FILES_INFOS_FIXTURE_PATH_FORMAT, formatVersion);
        String dataBlocksPath = String.format(DATA_BLOCKS_FIXTURE_PATH_FORMAT, formatVersion);

        NotesJsonMapper notesMapper = new NotesJsonMapper();
        FilesInfosJsonMapper filesInfosMapper = new FilesInfosJsonMapper();
        DataBlocksJsonMapper dataBlocksMapper = new DataBlocksJsonMapper();

        List<Note> expectedNotes = notesMapper.map(readFixture(notesPath, tempDir));
        List<Note> actualNotes = notesMapper.map(
            DbUtils.serializeTableAsJson(db.getConnection(), NOTES_TABLE_NAME));

        List<FileInfo> expectedFilesInfos =
            filesInfosMapper.map(readFixture(filesInfosPath, tempDir));

        List<FileInfo> actualFilesInfos =
                filesInfosMapper.map(
                    DbUtils.serializeTableAsJson(db.getConnection(), FILES_INFOS_TABLE_NAME));

        List<DataBlock> expectedDataBlocks =
            dataBlocksMapper.map(readFixture(dataBlocksPath, tempDir));

        List<DataBlock> actualDataBlocks =
                dataBlocksMapper.map(
                    DbUtils.serializeTableAsJson(db.getConnection(), DATA_BLOCKS_TABLE_NAME));

        assertEquals(expectedNotes, actualNotes, "Notes are different");
        assertEquals(expectedFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(expectedDataBlocks, actualDataBlocks, "Data blocks are different");
    }

    private CryptoSecrets getTestSecrets() throws IOException {
        String keyHex = Files.readString(getFixturePath("shared/crypto_key.txt", tempDir));
        return new CryptoSecrets(getKeyBytesFromHex(keyHex));
    }
}
