package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteFileInfoDtoDao;
import app.notesr.cli.dto.NotesTableRowDto;
import app.notesr.cli.util.UuidShortener;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@Slf4j
@CommandLine.Command(name = "list-notes",
        description = "Lists all notes stored in the NoteSR Backup Database.")
public final class ListNotesCommand extends Command {
    static final int MAX_NAME_LENGTH = 30;
    static final int MAX_TEXT_LENGTH = 30;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Option(names = { "-f", "--full-ids" }, description = "display full notes IDs")
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
            Set<NotesTableRowDto> tableRows = getTableRows(dbFile);

            if (!tableRows.isEmpty()) {
                out.println(getTable(tableRows));
            } else {
                log.info("{}: No notes", dbPath);
            }

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        }

        return exitCode;
    }

    private Set<NotesTableRowDto> getTableRows(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        NoteFileInfoDtoDao noteFileInfoDtoDao = new NoteFileInfoDtoDao(db);

        try {
            return noteFileInfoDtoDao.getNotesTable();
        } catch (SQLException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private String getTable(Set<NotesTableRowDto> tableDtoRows) {
        List<String> headers = List.of("ID", "Name", "Text", "Last update", "Files attached");
        List<List<String>> rows = tableDtoRows.stream()
                .map(dto -> List.of(
                        validateNoteId(dto.getNoteId()),
                        truncateText(dto.getNoteShortName(), MAX_NAME_LENGTH),
                        truncateText(dto.getNoteShortText(), MAX_TEXT_LENGTH),
                        dateTimeToString(dto.getNoteUpdatedAt()),
                        dto.getAttachedFilesCount().toString()
                )).toList();

        TablePrinter tablePrinter = new TablePrinter();
        return tablePrinter.printTable(headers, rows);
    }

    private String validateNoteId(String noteId) {
        UuidShortener uuidShortener = new UuidShortener(noteId);
        return displayFullNotesIds ? uuidShortener.getLongUuid() : uuidShortener.getShortUuid();
    }
}
