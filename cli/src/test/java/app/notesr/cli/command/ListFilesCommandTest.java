package app.notesr.cli.command;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.command.Command.DB_ERROR;
import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.command.Command.truncateText;
import static app.notesr.cli.command.ListFilesCommand.MAX_FILE_NAME_LENGTH;
import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;
import static app.notesr.cli.util.FileUtils.getReadableSize;
import static app.notesr.cli.util.test.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListFilesCommandTest {
    private static final Random RANDOM = new Random();
    private static final String BLANK_UUID = "123e4567-e89b-12d3-a456-426614174000";

    private CommandLine cmd;
    private ByteArrayOutputStream outputStream;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        ListFilesCommand listFilesCommand = new ListFilesCommand();

        cmd = new CommandLine(listFilesCommand);
        outputStream = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(outputStream, true));
        listFilesCommand.setOut(new PrintStream(outputStream));
    }

    @Test
    void testCommand() {
        Path dbPath = getFixturePath("shared/backup.db", tempDir);
        AbstractMap.SimpleEntry<Note, Set<FileInfo>> noteAttachmentsEntry = getRandomNoteWithAttachments(dbPath);

        Note testNote = noteAttachmentsEntry.getKey();
        Set<FileInfo> testFilesInfos = noteAttachmentsEntry.getValue();

        int exitCode = cmd.execute(dbPath.toString(), testNote.getId());
        String output = outputStream.toString();

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
        assertFiles(testFilesInfos, output);
    }

    @Test
    void testCommandWithInvalidDbPath() {
        Path dbPath = Path.of("/some/invalid/path/to/db");
        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithEmptyDb() {
        Path dbPath = getFixturePath("empty-backup.db", tempDir);

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(DB_ERROR, exitCode, "Expected code " + DB_ERROR);
    }

    private void assertFiles(Set<FileInfo> testFilesInfos, String receivedOutput) {
        for (FileInfo expected : testFilesInfos) {
            String expectedId = expected.getId();

            assertTrue(receivedOutput.contains(expectedId), "File id '"
                    + expectedId + "' not found in the output");

            String expectedFileName = truncateText(expected.getName(), MAX_FILE_NAME_LENGTH);
            assertTrue(receivedOutput.contains(expectedFileName),
                    "File name '" + expectedFileName + "' not found in the output");

            String expectedFileSize = getReadableSize(expected.getSize());
            assertTrue(receivedOutput.contains(expectedFileSize),
                    "File size '" + expectedFileSize + "' not found in the output");

            String expectedLastUpdateTime = dateTimeToString(expected.getUpdatedAt());
            assertTrue(receivedOutput.contains(expectedLastUpdateTime),
                    "Last update time '" + expectedLastUpdateTime + "' not found in the output");
        }
    }

    private AbstractMap.SimpleEntry<Note, Set<FileInfo>> getRandomNoteWithAttachments(Path dbPath) {
        DbConnection db = new DbConnection(dbPath.toString());

        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);

        List<Note> notes = new ArrayList<>(noteEntityDao.getAll());

        Note note = notes.get(RANDOM.nextInt(notes.size()));
        Set<FileInfo> filesInfos = fileInfoEntityDao.getByNoteId(note.getId());

        return new AbstractMap.SimpleEntry<>(note, filesInfos);
    }
}
