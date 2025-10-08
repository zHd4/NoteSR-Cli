package app.notesr.cli.command;

import app.notesr.cli.db.ConnectionException;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.exception.ThumbnailExtractionException;
import app.notesr.cli.service.FileAttachService;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

@Slf4j
@CommandLine.Command(name = "put-file",
        description = "Attaches a file to a specific note stored in a NoteSR Backup Database.")
public final class PutFileCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "db_path", description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id", description = "note id")
    private String noteId;

    @CommandLine.Parameters(index = "2", paramLabel = "file_path", description = "path to file to attach")
    private String filePath;

    public PutFileCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            File fileToPut = getFile(filePath);
            log.info("Adding file {}", fileToPut.getAbsolutePath());
            putFile(dbFile, fileToPut);
            log.info("File added successfully");
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

    private void putFile(File dbFile, File fileToPut) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());

        try {
            FileAttachService service = new FileAttachService(db);
            service.attachFile(fileToPut, noteId);
        } catch (NoteNotFoundException e) {
            log.error("{}: note with id {} not found", dbPath, noteId);
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        } catch (IOException e) {
            log.error("{}: failed to read file, details:\n{}", fileToPut.getAbsolutePath(), e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (ThumbnailExtractionException e) {
            log.error("{}: failed to extract thumbnail, details:\n{}", fileToPut.getAbsolutePath(), e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (UnableToExecuteStatementException e) {
            log.error("{}: failed to access database, details:\n{}", dbPath, e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        }
    }
}
