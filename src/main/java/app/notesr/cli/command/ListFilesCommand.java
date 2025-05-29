package app.notesr.cli.command;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoDtoDao;
import app.notesr.cli.dto.FilesTableRowDto;
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
import java.text.DecimalFormat;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@Slf4j
@CommandLine.Command(name = "list-files",
        description = "Lists attached files to a specific note.")
public final class ListFilesCommand extends Command {
    private static final int MAX_TABLE_ROW_WIDTH = 182;
    static final int MAX_FILE_NAME_LENGTH = 30;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @CommandLine.Option(names = { "-f", "--full-ids" }, description = "display full files IDs")
    private boolean displayFullFilesIds;

    @Setter(AccessLevel.PACKAGE)
    private PrintStream out = System.out;

    public ListFilesCommand() {
        super(log);
    }

    @Override
    public Integer call() throws Exception {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            Set<FilesTableRowDto> tableRows = getTableRows(dbFile);

            if (!tableRows.isEmpty()) {
                AsciiTable table = getAsciiTable(tableRows);
                out.println(table.render());
            } else {
                log.info("{}: The note does not contain attachments", validateFileId(noteId));
            }

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        }

        return exitCode;
    }

    private Set<FilesTableRowDto> getTableRows(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        FileInfoDtoDao fileInfoDtoDao = new FileInfoDtoDao(db);

        UuidShortener noteIdShortener = new UuidShortener(noteId);
        String fullNoteId = noteIdShortener.getLongUuid();

        try {
            return fileInfoDtoDao.getFilesTableRowsByNoteId(fullNoteId);
        } catch (SQLException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private AsciiTable getAsciiTable(Set<FilesTableRowDto> tableRows) {
        AsciiTable table = new AsciiTable();

        table.getContext().setGridTheme(TA_GridThemes.FULL);
        table.getContext().setWidth(MAX_TABLE_ROW_WIDTH);

        table.addRule();
        table.addRow("ID", "Name", "Size", "Last update");
        table.addRule();

        tableRows.forEach(row -> table.addRow(
                validateFileId(row.getId()),
                truncateText(row.getFileName(), MAX_FILE_NAME_LENGTH),
                getReadableSize(row.getFileSize()),
                dateTimeToString(row.getUpdatedAt())
        ));

        table.addRule();
        return table;
    }

    private String validateFileId(String fileId) {
        UuidShortener uuidShortener = new UuidShortener(fileId);
        return displayFullFilesIds ? uuidShortener.getLongUuid() : uuidShortener.getShortUuid();
    }

    static String getReadableSize(long size) {
        String[] units = new String[] { "B", "KB", "MB", "GB", "TB", "PB", "EB" };

        if(size > 0) {
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#")
                    .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        return "0 B";
    }
}
