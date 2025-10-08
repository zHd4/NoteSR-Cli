package app.notesr.cli.command;

import app.notesr.cli.db.ConnectionException;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.service.FileExtractionService;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.MappingException;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@CommandLine.Command(name = "get-file",
        description = "Saves a file attached to a specific note stored in a NoteSR Backup Database.")
public final class GetFileCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "db_path", description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "file_id", description = "file id")
    private String fileId;

    @CommandLine.Option(names = {"-o", "--output"}, description = "output file path")
    private String outputFilePath;

    public GetFileCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        try {
            File dbFile = getFile(dbPath);
            extractFile(dbFile);
            return SUCCESS;
        } catch (CommandHandlingException e) {
            return e.getExitCode();
        } catch (ConnectionException e) {
            log.error(e.getMessage());
            log.debug("E: ", e);
            return DB_ERROR;
        }
    }

    private void extractFile(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());

        try {
            FileExtractionService service = new FileExtractionService(db);
            FileInfo fileInfo = service.getFileInfo(fileId);

            if (fileInfo == null) {
                log.error("{}: file with id '{}' not found", dbPath, fileId);
                throw new CommandHandlingException(DB_ERROR);
            }

            File outputFile = resolveOutputFile(dbFile, fileInfo.getName());

            log.info("Saving file {} with id {}", fileInfo.getName(), fileInfo.getId());
            service.extractFile(fileId, outputFile);
            log.info("Saved successfully to: {}", outputFile.getAbsolutePath());
        } catch (MappingException | UnableToProduceResultException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        } catch (IOException e) {
            log.error("I/O error while saving: {}", e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private File resolveOutputFile(File dbFile, String originalName) throws CommandHandlingException {
        try {
            Path path = outputFilePath == null
                    ? getParentDirPath(dbFile.toPath()).resolve(originalName)
                    : Path.of(outputFilePath);

            if (Files.isDirectory(path)) {
                path = path.resolve(originalName);
            }

            File outFile = path.toFile();

            if (outFile.exists()) {
                throw new FileAlreadyExistsException(path.toString());
            }

            return outFile;
        } catch (FileAlreadyExistsException e) {
            log.error("{}: file already exists", e.getFile());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during path resolution: {}", e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(UNKNOWN_ERROR);
        }
    }

    private Path getParentDirPath(Path filePath) {
        Path absolutePath = filePath.toAbsolutePath();
        Path parent = absolutePath.getParent();

        return parent != null ? parent : absolutePath.getRoot();
    }
}
