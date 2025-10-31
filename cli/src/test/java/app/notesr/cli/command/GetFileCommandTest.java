package app.notesr.cli.command;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.command.Command.DB_ERROR;
import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.util.test.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.test.HashUtils.computeSha512;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetFileCommandTest {
    private static final String BLANK_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final Random RANDOM = new Random();

    private CommandLine cmd;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        GetFileCommand getFileCommand = new GetFileCommand();
        cmd = new CommandLine(getFileCommand);
    }

    @Test
    void testCommand() throws NoSuchAlgorithmException, IOException {
        Path dbPath = getFixturePath("shared/backup.db", tempDir);
        Path outputPath = tempDir.resolve("output_file");

        DbConnection db = new DbConnection(dbPath.toString());

        String testFileId = getTestFileId(db);
        List<byte[]> testFileData = getFileData(db, testFileId);

        int exitCode = cmd.execute(dbPath.toString(), testFileId, "--output", outputPath.toString());

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
        assertTrue(Files.exists(outputPath), "Output file " + outputPath + " not found");

        String expectedHash = computeSha512(testFileData);
        String actualHash = computeSha512(outputPath.toString());

        assertEquals(expectedHash, actualHash, "Unexpected output file hash");
    }

    @Test
    void testCommandWithInvalidDbPath() {
        Path dbPath = Path.of("/////some///weird//path///file");

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithInvalidDb() throws IOException {
        Path dbPath = tempDir.resolve("invalid_db.db");
        Files.writeString(dbPath, "Lorem ipsum");

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(DB_ERROR, exitCode, "Expected code " + DB_ERROR);
    }

    @Test
    void testCommandWithInvalidFileId() {
        Path dbPath = getFixturePath("shared/backup.db", tempDir);

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(DB_ERROR, exitCode, "Expected code " + DB_ERROR);
    }

    private String getTestFileId(DbConnection db) {
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        List<FileInfo> filesInfos = new ArrayList<>(fileInfoEntityDao.getAll());

        return filesInfos.get(RANDOM.nextInt(filesInfos.size())).getId();
    }

    private List<byte[]> getFileData(DbConnection db, String fileId) {
        DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);
        Set<String> dataBlocksIds = dataBlockEntityDao.getIdsByFileId(fileId);

        return dataBlocksIds.stream()
                .map(id -> requireNonNull(dataBlockEntityDao.getById(id)).getData())
                .toList();
    }
}
