package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteFileInfoDao;
import app.notesr.cli.dto.NotesTableDto;
import app.notesr.cli.util.UuidShortener;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@Slf4j
@CommandLine.Command(name = "list-notes",
        description = "Lists all notes stored in the NoteSR Backup Database.")
public final class ListNotesCommand extends Command {
    private static final int MAX_TABLE_ROW_WIDTH = 182;

    static final int MAX_NAME_LENGTH = 30;
    static final int MAX_TEXT_LENGTH = 30;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Option(names = { "-f", "--full-ids" }, description = "Display full notes IDs")
    private boolean displayFullNotesIds;

    @Setter(AccessLevel.PACKAGE)
    private PrintStream out = System.out;

    public ListNotesCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            Set<NotesTableDto> tableRows = getTableRows(dbFile);

            if (!tableRows.isEmpty()) {
                AsciiTable table = getAsciiTable(tableRows);
                out.println(table.render());
            } else {
                log.info("{}: No notes", dbPath);
            }

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        }

        return exitCode;
    }

    private Set<NotesTableDto> getTableRows(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        NoteFileInfoDao noteFileInfoDao = new NoteFileInfoDao(db);

        try {
            return noteFileInfoDao.getNoteFileInfoOutputTable();
        } catch (SQLException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private AsciiTable getAsciiTable(Set<NotesTableDto> tableRows) {
        AsciiTable table = new AsciiTable();

        table.getContext().setGridTheme(TA_GridThemes.FULL);
        table.getContext().setWidth(MAX_TABLE_ROW_WIDTH);

        table.addRule();
        table.addRow("ID", "Name", "Text", "Last update", "Files attached");
        table.addRule();

        tableRows.forEach(row -> table.addRow(
                validateNoteId(row.getNoteId()),
                truncate(row.getNoteShortName(), MAX_NAME_LENGTH),
                truncate(row.getNoteShortText(), MAX_TEXT_LENGTH),
                dateTimeToString(row.getNoteUpdatedAt()),
                row.getAttachedFilesCount()
        ));

        table.addRule();
        return table;
    }

    private String validateNoteId(String noteId) {
        UuidShortener uuidShortener = new UuidShortener(noteId);
        return displayFullNotesIds ? uuidShortener.getLongUuid() : uuidShortener.getShortUuid();
    }

    static String truncate(String text, int maxLength) {
        if (text.length() < maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 1) + "…";
    }
}
