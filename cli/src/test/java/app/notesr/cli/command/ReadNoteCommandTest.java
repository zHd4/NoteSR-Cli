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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static app.notesr.cli.command.Command.DB_ERROR;
import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadNoteCommandTest {
    private static final Random RANDOM = new Random();
    private static final String BLANK_UUID = "123e4567-e89b-12d3-a456-426614174000";

    private CommandLine cmd;
    private ByteArrayOutputStream outputStream;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        ReadNoteCommand readNoteCommand = new ReadNoteCommand();

        cmd = new CommandLine(readNoteCommand);
        outputStream = new ByteArrayOutputStream();
        cmd.setOut(new PrintWriter(outputStream, true));
        readNoteCommand.setOut(new PrintStream(outputStream));
    }

    @Test
    void testCommand() {
        Path dbPath = getFixturePath("backup.db", tempDir);
        Note testNote = getTestNote(dbPath);

        int exitCode = cmd.execute(dbPath.toString(), testNote.getId());
        String output = outputStream.toString();

        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);

        assertTrue(output.contains(testNote.getId()),
                "Note id not found (ID: " + testNote.getId() + ")");

        assertTrue(output.contains(wrapText(testNote.getName())),
                "Note name not found (ID: " + testNote.getId() + ")");

        assertTrue(output.contains(wrapText(testNote.getText())),
                "Note text note found (ID: " + testNote.getId() + ")");

        assertTrue(output.contains(dateTimeToString(testNote.getUpdatedAt())),
                "Last update time of note not found (ID: " + testNote.getId() + ")");
    }

    @Test
    void testCommandWithInvalidDbPath() {
        Path dbPath = Path.of("/////some///weird//path///file");

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testCommandWithInvalidNoteId() {
        Path dbPath = getFixturePath("backup.db", tempDir);

        int exitCode = cmd.execute(dbPath.toString(), BLANK_UUID);
        assertEquals(DB_ERROR, exitCode, "Expected code " + DB_ERROR);
    }

    private String wrapText(String text) {
        return String.join("\n", ReadNoteCommand.wrapText(text));
    }

    private Note getTestNote(Path dbPath) {
        DbConnection db = new DbConnection(dbPath.toString());
        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);

        List<Note> notes = new ArrayList<>(noteEntityDao.getAll());
        return notes.get(RANDOM.nextInt(notes.size()));
    }
}
