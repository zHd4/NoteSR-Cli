package app.notesr.cli.command;

import app.notesr.cli.db.ConnectionException;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.util.UuidShortener;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;

@Slf4j
@CommandLine.Command(name = "put-file",
        description = "Attaches a file to a specific note stored in a NoteSR Backup Database.")
public final class PutFileCommand extends Command {
    private static final int CHUNK_SIZE = 500000;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @CommandLine.Parameters(index = "2", paramLabel = "file_path",
            description = "path to file to attach")
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

            putFile(dbFile, fileToPut);

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } catch (ConnectionException e) {
            log.error(e.getMessage());
            exitCode = DB_ERROR;
        }

        return exitCode;
    }

    private void putFile(File dbFile, File fileToPut) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());

        String fullNoteId = new UuidShortener(noteId).getLongUuid();
        FileInfo fileInfo;

        try {
            if (!isNoteExists(db, fullNoteId)) {
                log.error("{}: note with id {} not found", dbPath, noteId);
            }
        } catch (SQLException e) {
            log.error("{}: failed to fetch note id from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }

        try {
            FileInfoEntityDao fileInfoEntityDao = new FileInfoEntityDao(db);
            fileInfo = getFileInfo(fileToPut, fullNoteId);
            fileInfoEntityDao.add(fileInfo);
        } catch (IOException e) {
            log.error("{}: failed to fetch file info, details:\n{}", fileToPut.getAbsolutePath(), e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (SQLException e) {
            log.error("{}: failed to write file info to database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }

        try {
            DataBlockEntityDao dataBlockEntityDao = new DataBlockEntityDao(db);
            putFileData(dataBlockEntityDao, fileInfo.getId(), fileToPut);
        } catch (IOException e) {
            log.error("{}: failed to read file data, details:\n{}", fileToPut.getAbsolutePath(), e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (SQLException e) {
            log.error("{}: failed to write file data to database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private void putFileData(DataBlockEntityDao dataBlockEntityDao, String fileId, File file)
            throws IOException, SQLException {
        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] chunk = new byte[CHUNK_SIZE];

            long order = 0;
            int bytesRead = stream.read(chunk);

            while (bytesRead != -1) {
                if (bytesRead != CHUNK_SIZE) {
                    byte[] subChunk = new byte[bytesRead];
                    System.arraycopy(chunk, 0, subChunk, 0, bytesRead);
                    chunk = subChunk;
                }

                DataBlock dataBlock = DataBlock.builder()
                        .id(randomUUID().toString())
                        .fileId(fileId)
                        .order(order)
                        .data(chunk)
                        .build();

                dataBlockEntityDao.add(dataBlock);

                chunk = new byte[CHUNK_SIZE];
                bytesRead = stream.read(chunk);

                order++;
            }
        }
    }

    private FileInfo getFileInfo(File file, String noteId) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        return FileInfo.builder()
                .id(randomUUID().toString())
                .noteId(noteId)
                .name(file.getName())
                .type(Files.probeContentType(file.toPath()))
                .size(Files.size(file.toPath()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private boolean isNoteExists(DbConnection db, String id) throws SQLException {
        NoteEntityDao noteEntityDao = new NoteEntityDao(db);
        return noteEntityDao.getById(id) != null;
    }
}
