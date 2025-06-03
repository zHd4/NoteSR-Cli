package app.notesr.cli.command;

import app.notesr.cli.db.ConnectionException;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.util.UuidShortener;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;

@Slf4j
@CommandLine.Command(name = "get-file",
        description = "Saves a file attached to a specific note stored in a NoteSR Backup Database.")
public final class GetFileCommand extends Command {
    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "file_id",
            description = "file id")
    private String fileId;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output file path")
    private String outputFilePath;

    public GetFileCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            saveFile(dbFile);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } catch (ConnectionException e) {
            log.error(e.getMessage());
            exitCode = DB_ERROR;
        }

        return exitCode;
    }

    private void saveFile(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        FileInfoEntityDao fileInfoEntityDao = new FileInfoEntityDao(db);

        UuidShortener fileIdShortener = new UuidShortener(fileId);

        try {
            String fullFileId = fileIdShortener.getLongUuid();
            FileInfo fileInfo = fileInfoEntityDao.getById(fullFileId);

            if (fileInfo == null) {
                log.error("{}: file with id '{}' not found", dbPath, fileId);
                throw new CommandHandlingException(DB_ERROR);
            }

            File outputFile = getOutputFile(dbFile, fileInfo.getName());

            log.info("Saving file {} with id {}", fileInfo.getName(), fileInfo.getId());
            writeFileData(db, fullFileId, outputFile);
            log.info("Saved successfully");
        } catch (SQLException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        } catch (IOException e) {
            log.error("{}: failed to save, details:\n{}", outputFilePath, e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private File getOutputFile(File dbFile, String originalFileName) throws CommandHandlingException {
        File outputFile;

        try {
            Path outputFilePathObj;

            if (outputFilePath == null) {
                outputFilePathObj = dbFile.getParentFile().toPath().resolve(originalFileName);
            } else {
                outputFilePathObj = Path.of(outputFilePath);
            }

            outputFile = outputFilePathObj.toFile();

            if (outputFile.exists()) {
                throw new FileAlreadyExistsException(outputFilePathObj.toString());
            }
        } catch (FileAlreadyExistsException e) {
            log.error("{}: file already exists", e.getFile());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (NullPointerException e) {
            log.error("Unknown error, details: {}", e.getMessage());
            throw new CommandHandlingException(UNKNOWN_ERROR);
        }

        return outputFile;
    }

    private void writeFileData(DbConnection db, String fileId, File outputFile) throws SQLException, IOException {
        DataBlockEntityDao dataBlockEntityDao = new DataBlockEntityDao(db);
        Set<String> dataBlocksIds = dataBlockEntityDao.getIdsByFileId(fileId);

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            for (String dataBlockId : dataBlocksIds) {
                DataBlock dataBlock = dataBlockEntityDao.getById(dataBlockId);

                if (dataBlock == null) {
                    throw new NullPointerException("Data block with id " + dataBlockId
                            + " not found, possible file database corrupted");
                }

                byte[] data = dataBlock.getData();
                outputStream.write(data);
            }
        }
    }
}
