package app.notesr.cli.command;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.data.model.DataBlock;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.command.Command.DB_ERROR;
import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.util.test.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.test.HashUtils.computeSha512;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PutFileCommandTest {
    private static final String BLANK_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final Random RANDOM = new Random();
    private static final int MAX_TEST_FILE_BYTES = 1024 * 100;

    private CommandLine cmd;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        PutFileCommand putFileCommand = new PutFileCommand();
        cmd = new CommandLine(putFileCommand);
    }

    @Test
    void testCommand() throws IOException, NoSuchAlgorithmException {
        Path dbPath = getFixturePath("backup.db", tempDir);
        Path testFilePath = tempDir.resolve("test_file");
        Note testNote = getRandomNote(dbPath);

        byte[] testFileBytes = new byte[RANDOM.nextInt(1, MAX_TEST_FILE_BYTES)];

        RANDOM.nextBytes(testFileBytes);
        Files.write(testFilePath, testFileBytes);

        int exitCode = cmd.execute(dbPath.toString(), testNote.getId(), testFilePath.toString());

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
        assertFileAdded(dbPath, testFilePath, testNote.getId());
    }

    @Test
    void testCommandWithInvalidDbPath() {
        String weirdPath = Path.of("/////some///weird//path///file").toString();

        int exitCode = cmd.execute(weirdPath, BLANK_UUID, weirdPath);
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithInvalidDb() throws IOException {
        Path dbPath = tempDir.resolve("invalid_db.db");
        Path filePath = tempDir.resolve("file.txt");

        Files.writeString(dbPath, "Lorem ipsum");
        Files.writeString(filePath, "Hello world!");

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID, filePath.toString());
        assertEquals(DB_ERROR, exitCode, "Expected code " + DB_ERROR);
    }

    private void assertFileAdded(Path dbPath, Path filePath, String noteId) throws IOException,
            NoSuchAlgorithmException {
        DbConnection db = new DbConnection(dbPath.toString());
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);

        String fileName = filePath.toFile().getName();
        long fileSize = Files.size(filePath);
        FileInfo fileInfo = getFileInfo(fileInfoEntityDao, noteId, fileName);

        assertNotNull(fileInfo, "File info with name '" + fileName + "' and note id " + noteId + " not found");
        assertEquals(fileSize, fileInfo.getSize(), "Unexpected file size");

        Set<DataBlock> fileDataBlocks = getDataBlocks(dataBlockEntityDao, fileInfo.getId());
        assertFalse(fileDataBlocks.isEmpty(), "No file data blocks found by file id "
                + fileInfo.getId() + " (" + fileName + ")");

        assertFileHash(filePath, fileDataBlocks);

    }

    private void assertFileHash(Path filePath, Set<DataBlock> fileDataBlocks)
            throws NoSuchAlgorithmException, IOException {
        String expectedFileHash = computeSha512(filePath.toString());
        String actualFileHash = computeSha512(fileDataBlocks.stream().map(DataBlock::getData).toList());

        assertEquals(expectedFileHash, actualFileHash, "Unexpected file hash");
    }

    private FileInfo getFileInfo(FileInfoEntityDao fileInfoEntityDao, String noteId, String fileName) {
        return fileInfoEntityDao.getByNoteId(noteId).stream()
                .filter(file -> fileName.equals(file.getName()))
                .findAny()
                .orElse(null);
    }

    private Set<DataBlock> getDataBlocks(DataBlockEntityDao dataBlockEntityDao, String fileId) {
        Set<DataBlock> dataBlocks = new LinkedHashSet<>();

        for (String dataBlockId : dataBlockEntityDao.getIdsByFileId(fileId)) {
            dataBlocks.add(dataBlockEntityDao.getById(dataBlockId));
        }

        return dataBlocks;
    }

    private Note getRandomNote(Path dbPath) {
        DbConnection db = new DbConnection(dbPath.toString());
        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        List<Note> notes = new ArrayList<>(noteEntityDao.getAll());

        return notes.get(RANDOM.nextInt(notes.size()));
    }
}
