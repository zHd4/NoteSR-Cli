package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.mapper.DataBlocksJsonMapper;
import app.notesr.cli.util.mapper.FilesInfosJsonMapper;
import app.notesr.cli.util.mapper.JsonMapper;
import app.notesr.cli.util.mapper.NotesJsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static app.notesr.cli.command.DecryptCommand.FILE_RW_ERROR;
import static app.notesr.cli.command.DecryptCommand.SUCCESS;
import static app.notesr.cli.util.DbUtils.serializeTableAsJson;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecryptCommandTest {
    private static final String FORMAT_V1 = "v1";
    private static final String FORMAT_V2 = "v2";

    private static final Random RANDOM = new Random();

    private CommandLine cmd;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        DecryptCommand decryptCommand = new DecryptCommand();
        cmd = new CommandLine(decryptCommand);
    }

    @Test
    void testWithoutArgs() {
        int exitCode = cmd.execute();
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C:\\folder\\..\\NUL\\file", "/////some///weird//path///file"})
    void testWithInvalidFilesPaths(String path) {
        String backupPath = path + ".notesr.bak";
        String keyPath = path + ".txt";

        int exitCode = cmd.execute(backupPath, keyPath);
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testWithInvalidKeyAsString() throws IOException {
        String invalidKey = "TEST_INVALID_KEY";

        Path invalidKeyPath = tempDir.resolve("invalid_key.txt");
        Path backupPath = getFixturePath(String.format("encrypted-%s.notesr.bak", FORMAT_V2));

        Files.writeString(invalidKeyPath, invalidKey);
        int exitCode = cmd.execute(backupPath.toString(), invalidKeyPath.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testWithInvalidKeyAsBinary() throws IOException {
        byte[] invalidKey = new byte[1024];
        RANDOM.nextBytes(invalidKey);

        Path invalidKeyPath = tempDir.resolve("invalid_key.txt");
        Path backupPath = getFixturePath(String.format("encrypted-%s.notesr.bak", FORMAT_V2));

        Files.write(invalidKeyPath, invalidKey);
        int exitCode = cmd.execute(backupPath.toString(), invalidKeyPath.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {FORMAT_V1, FORMAT_V2})
    void testWithAllArgs(String formatVersion) throws IOException, SQLException {
        final String notesTableName = "notes";
        final String filesInfosTableName = "files_info";
        final String dataBlocksTableName = "data_blocks";

        Path backupPath = getFixturePath(String.format("encrypted-%s.notesr.bak", formatVersion));
        Path keyPath = getFixturePath("crypto_key.txt");
        Path outputPath = tempDir.resolve(backupPath.getFileName().toString() + ".db");

        int exitCode = cmd.execute(backupPath.toString(), keyPath.toString(), "-o", outputPath.toString());

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
        assertTrue(outputPath.toFile().exists(), "Output file " + outputPath + " not found");

        DbConnection db = new DbConnection(outputPath.toString());

        NotesJsonMapper notesMapper = new NotesJsonMapper();
        FilesInfosJsonMapper filesInfosMapper = new FilesInfosJsonMapper();
        DataBlocksJsonMapper dataBlocksMapper = new DataBlocksJsonMapper();

        List<Note> expectedNotes = getExpectedModels(notesMapper, "expected-notes.json", formatVersion);
        List<Note> actualNotes = notesMapper.map(serializeTableAsJson(db.getConnection(), notesTableName));

        List<FileInfo> expectedFilesInfos =
                getExpectedModels(filesInfosMapper, "expected-files-infos.json", formatVersion);
        List<FileInfo> actualFilesInfos =
                filesInfosMapper.map(serializeTableAsJson(db.getConnection(), filesInfosTableName));


        List<DataBlock> expectedDataBlocks =
                getExpectedModels(dataBlocksMapper, "expected-data-blocks.json", formatVersion);
        List<DataBlock> actualDataBlocks =
                dataBlocksMapper.map(serializeTableAsJson(db.getConnection(), dataBlocksTableName));

        assertEquals(expectedNotes, actualNotes, "Notes are different");
        assertEquals(expectedFilesInfos, actualFilesInfos, "Files infos are different");
        assertEquals(expectedDataBlocks, actualDataBlocks, "Data blocks are different");
    }

    private static <T> List<T> getExpectedModels(JsonMapper<T> mapper, String fixtureName, String formatVersion)
            throws IOException {
        String fixturePath = Path.of("parser", formatVersion, fixtureName).toString();
        String json = readFixture(fixturePath);

        return mapper.map(json);
    }
}
