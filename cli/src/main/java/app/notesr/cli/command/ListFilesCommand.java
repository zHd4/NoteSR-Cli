package app.notesr.cli.command;

import app.notesr.cli.data.ConnectionException;
import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dto.FilesTableRowDto;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.service.FilesListingService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.MappingException;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;
import static app.notesr.cli.util.FileUtils.getReadableSize;

@Slf4j
@CommandLine.Command(name = "list-files",
        description = "Lists attached files to a specific note stored in a NoteSR Backup Database.")
public final class ListFilesCommand extends Command {
    static final int MAX_FILE_NAME_LENGTH = 30;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @Setter(AccessLevel.PACKAGE)
    private PrintStream out = System.out;

    public ListFilesCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            Set<FilesTableRowDto> tableRows = getTableRows(dbFile);

            if (!tableRows.isEmpty()) {
                out.println(getTable(tableRows));
            } else {
                log.info("{}: The note does not contain attachments", noteId);
            }

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } catch (ConnectionException e) {
            log.error(e.getMessage());
            log.debug("E: ", e);
            exitCode = DB_ERROR;
        }

        return exitCode;
    }

    private Set<FilesTableRowDto> getTableRows(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        FilesListingService filesListingService = new FilesListingService(db);

        try {
            return filesListingService.listFiles(noteId);
        } catch (MappingException | UnableToProduceResultException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        } catch (NoteNotFoundException e) {
            log.error(e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private String getTable(Set<FilesTableRowDto> tableDtoRows) {
        List<String> headers = List.of("ID", "Name", "Size", "Last update");
        List<List<String>> rows = tableDtoRows.stream()
                .map(dto -> List.of(
                        dto.getId(),
                        truncateText(dto.getFileName(), MAX_FILE_NAME_LENGTH),
                        getReadableSize(dto.getFileSize()),
                        dateTimeToString(dto.getUpdatedAt())
                        )).toList();

        TableRenderer tableRenderer = new TableRenderer();
        return tableRenderer.render(headers, rows);
    }
}
