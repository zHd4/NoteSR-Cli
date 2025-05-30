package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.dto.NoteOutputDto;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.UuidShortener;
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
            DbConnection db = new DbConnection(dbFile.getAbsolutePath());
            NoteOutputDto noteOutputDto = getNoteOutputDto(db);

            renderNote(noteOutputDto);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        }

        return exitCode;
    }

    private void renderNote(NoteOutputDto noteOutputDto) {
        Note note = noteOutputDto.getNote();
        Long attachmentsCount = noteOutputDto.getAttachmentsCount();

        String separator = "─".repeat(LINE_WIDTH);

        out.println();
        out.println(separator);
        out.println(wrapText(note.getName()));
        out.println(separator);
        out.println(wrapText(note.getText()));
        out.println(separator);
        out.println("Full id: " + note.getId());
        out.println("Updated at: " + dateTimeToString(note.getUpdatedAt()));
        out.println("Attached files: " + attachmentsCount.toString());
        out.println(separator);
        out.println();
    }

    private NoteOutputDto getNoteOutputDto(DbConnection db) throws CommandHandlingException {
        NoteEntityDao noteEntityDao = new NoteEntityDao(db);
        FileInfoEntityDao fileInfoEntityDao = new FileInfoEntityDao(db);

        UuidShortener noteIdShortener = new UuidShortener(noteId);
        String fullNoteId = noteIdShortener.getLongUuid();

        try {
            Note note = noteEntityDao.getById(fullNoteId);
            Long attachmentsCount = fileInfoEntityDao.getCountByNoteId(fullNoteId);

            if (note == null) {
                log.error("{}: note with id '{}' not found", dbPath, noteId);
                throw new CommandHandlingException(DB_ERROR);
            }

            if (attachmentsCount == null) {
                throw new SQLException("Attachments count is null");
            }

            return new NoteOutputDto(note, attachmentsCount);
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
