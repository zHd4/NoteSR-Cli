package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteDao;
import app.notesr.cli.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;

import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.command.ListNotesCommand.MAX_NAME_LENGTH;
import static app.notesr.cli.command.ListNotesCommand.shortenId;
import static app.notesr.cli.command.ListNotesCommand.truncate;
import static app.notesr.cli.db.DbUtils.dateTimeToString;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListNotesCommandTest {
    private CommandLine cmd;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        ListNotesCommand listNotesCommand = new ListNotesCommand();

        cmd = new CommandLine(listNotesCommand);
        outputStream = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(outputStream, true));
        listNotesCommand.setOut(new PrintStream(outputStream));
    }

    @Test
    void testCommand() throws SQLException {
        Path dbPath = getFixturePath("backup.db");

        int exitCode = cmd.execute(dbPath.toString());
        String output = outputStream.toString();

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);

        for (Note expected : getAllNotesFromDb(dbPath)) {
            String expectedId = shortenId(expected.getId());
            assertTrue(output.contains(expectedId), "Note id '"
                    + expectedId + "' not found in the output");

            String expectedName = truncate(expected.getName(), MAX_NAME_LENGTH);
            assertTrue(output.contains(expectedName), "Name of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedName + "')");

            String expectedText = truncate(expected.getName(), MAX_NAME_LENGTH);
            assertTrue(output.contains(expectedText), "Text of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedText + "')");

            String expectedLastUpdateTime = dateTimeToString(expected.getUpdatedAt());
            assertTrue(output.contains(expectedLastUpdateTime), "Last update time of note (id='"
                    + expectedId + "') not found in the output (expected: '" + expectedLastUpdateTime + "')");
        }
    }

    @Test
    void testCommandWithInvalidDbPath() {
        Path dbPath = Path.of("/////some///weird//path///file");
        int exitCode = cmd.execute(dbPath.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithEmptyDb() {
        Path dbPath = getFixturePath("empty-backup.db");

        int exitCode = cmd.execute(dbPath.toString());
        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);
    }

    private Set<Note> getAllNotesFromDb(Path dbPath) throws SQLException {
        DbConnection db = new DbConnection(dbPath.toString());
        NoteDao noteDao = new NoteDao(db);
        return noteDao.getAll();
    }
}
