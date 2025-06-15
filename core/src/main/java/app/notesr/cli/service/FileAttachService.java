package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.util.ChunkedFileUploader;
import app.notesr.cli.util.UuidShortener;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
public final class FileAttachService {
    private final DbConnection db;

    public void attachFile(File file, String noteId) throws IOException, SQLException, NoteNotFoundException {
        String fullNoteId = new UuidShortener(noteId).getLongUuid();

        if (!isNoteExists(fullNoteId)) {
            throw new NoteNotFoundException(noteId);
        }

        FileInfo fileInfo = buildFileInfo(file, fullNoteId);
        new FileInfoEntityDao(db).add(fileInfo);

        DataBlockEntityDao dataBlockDao = new DataBlockEntityDao(db);
        ChunkedFileUploader fileUploader = new ChunkedFileUploader(dataBlockDao);

        fileUploader.upload(fileInfo.getId(), file);
    }

    private boolean isNoteExists(String fullNoteId) throws SQLException {
        NoteEntityDao dao = new NoteEntityDao(db);
        return dao.getById(fullNoteId) != null;
    }

    private FileInfo buildFileInfo(File file, String noteId) throws IOException {
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
}
