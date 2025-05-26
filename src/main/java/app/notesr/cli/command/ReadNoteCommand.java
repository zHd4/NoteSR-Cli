package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteDao;
import app.notesr.cli.model.Note;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@Slf4j
@CommandLine.Command(name = "read-note",
        description = "Reads a note stored in a NoteSR Backup Database.")
public final class ReadNoteCommand extends Command {
    private static final int LINE_WIDTH = 50;

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BOLD = "\u001B[1m";

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @Setter(AccessLevel.PACKAGE)
    private PrintStream out = System.out;

    public ReadNoteCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            Note note = getNote(dbFile);

            renderNote(note);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        }

        return exitCode;
    }

    private void renderNote(Note note) {
        String separator = "─".repeat(LINE_WIDTH);

        out.println();
        out.println(CYAN + separator + RESET);
        out.println(BOLD + "Title: " + RESET + YELLOW + note.getName() + RESET);
        out.println(CYAN + separator + RESET);
        out.println(BOLD + "Content:" + RESET);
        out.println(wrapText(note.getText()));
        out.println(CYAN + separator + RESET);
        out.println(BOLD + "Updated at: " + RESET + GREEN + dateTimeToString(note.getUpdatedAt()) + RESET);
        out.println(CYAN + separator + RESET);
        out.println();
    }

    private Note getNote(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        NoteDao noteDao = new NoteDao(db);

        try {
            Note note = noteDao.getById(noteId);

            if (note == null) {
                log.error("{}: note with id '{}' not found", dbPath, noteId);
                throw new CommandHandlingException(DB_ERROR);
            }

            return note;
        } catch (SQLException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    static String wrapText(String text) {
        StringBuilder builder = new StringBuilder();
        int pos = 0;

        while (pos < text.length()) {
            int end = Math.min(pos + LINE_WIDTH, text.length());
            builder.append(text, pos, end).append(System.lineSeparator());
            pos = end;
        }

        return builder.toString();
    }
}
