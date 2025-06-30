package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Set;

import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.command.ListNotesCommand.MAX_NAME_LENGTH;
import static app.notesr.cli.command.ListNotesCommand.truncateText;
import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListNotesCommandTest {
    private CommandLine cmd;
    private ByteArrayOutputStream outputStream;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        ListNotesCommand listNotesCommand = new ListNotesCommand();

        cmd = new CommandLine(listNotesCommand);
        outputStream = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(outputStream, true));
        listNotesCommand.setOut(new PrintStream(outputStream));
    }

    @Test
    void testCommand() {
        Path dbPath = getFixturePath("backup.db", tempDir);

        int exitCode = cmd.execute(dbPath.toString());
        String output = outputStream.toString();

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
        assertNotes(dbPath, output);
    }

    @Test
    void testCommandWithInvalidDbPath() {
        Path dbPath = Path.of("/////some///weird//path///file");
        int exitCode = cmd.execute(dbPath.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithEmptyDb() {
        Path dbPath = getFixturePath("empty-backup.db", tempDir);

        int exitCode = cmd.execute(dbPath.toString());
        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
    }

    private void assertNotes(Path dbPath, String receivedOutput) {
        for (Note expected : getAllNotesFromDb(dbPath)) {
            String expectedId = expected.getId();

            assertTrue(receivedOutput.contains(expectedId), "Note id '"
                    + expectedId + "' not found in the output");

            String expectedName = truncateText(expected.getName(), MAX_NAME_LENGTH);
            assertTrue(receivedOutput.contains(expectedName), "Name of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedName + "')");

            String expectedText = truncateText(expected.getName(), MAX_NAME_LENGTH);
            assertTrue(receivedOutput.contains(expectedText), "Text of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedText + "')");

            String expectedLastUpdateTime = dateTimeToString(expected.getUpdatedAt());
            assertTrue(receivedOutput.contains(expectedLastUpdateTime), "Last update time of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedLastUpdateTime + "')");
        }
    }

    private Set<Note> getAllNotesFromDb(Path dbPath) {
        DbConnection db = new DbConnection(dbPath.toString());
        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        return noteEntityDao.getAll();
    }
}
