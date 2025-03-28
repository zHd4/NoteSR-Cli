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
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static app.notesr.cli.command.DecryptCommand.FILE_RW_ERROR;
import static app.notesr.cli.command.DecryptCommand.SUCCESS;
import static app.notesr.cli.util.DbUtils.serializeTableAsJson;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.FixtureUtils.readFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DecryptCommandTest {
    private CommandLine cmd;

    @TempDir
    private Path tempDir;

    @BeforeEach
    public void beforeEach() {
        DecryptCommand decryptCommand = new DecryptCommand();
        cmd = new CommandLine(decryptCommand);
    }

    @Test
    public void testWithoutArgs() {
        int exitCode = cmd.execute();
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C:\\folder\\..\\NUL\\file", "/////some///weird//path///file"})
    public void testWithInvalidFilesPaths(String path) {
        String backupPath = path + ".notesr.bak";
        String keyPath = path + ".txt";

        int exitCode = cmd.execute(backupPath, keyPath);
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1", "v2"})
    public void testWithAllArgs(String formatVersion) throws IOException, SQLException {
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
