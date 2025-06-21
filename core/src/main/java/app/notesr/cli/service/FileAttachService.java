package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.util.ChunkedFileUploader;
import app.notesr.cli.util.MediaThumbnailUtils;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
public final class FileAttachService {
    private static final int THUMBNAIL_SIZE = 100;
    private static final int THUMBNAIL_VIDEO_SECONDS = 1;

    private final DbConnection db;

    public void attachFile(File file, String noteId) throws IOException, NoteNotFoundException {
        if (!isNoteExists(noteId)) {
            throw new NoteNotFoundException(noteId);
        }

        FileInfo fileInfo = buildFileInfo(file, noteId);

        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        DataBlockEntityDao dataBlockDao = db.getConnection().onDemand(DataBlockEntityDao.class);
        ChunkedFileUploader fileUploader = new ChunkedFileUploader(dataBlockDao);

        fileInfoEntityDao.add(fileInfo);
        fileUploader.upload(fileInfo.getId(), file);
    }

    private boolean isNoteExists(String fullNoteId) {
        NoteEntityDao dao = db.getConnection().onDemand(NoteEntityDao.class);
        return dao.getById(fullNoteId) != null;
    }

    private FileInfo buildFileInfo(File file, String noteId) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        return FileInfo.builder()
                .id(randomUUID().toString())
                .noteId(noteId)
                .name(file.getName())
                .type(Files.probeContentType(file.toPath()))
                .thumbnail(getFileThumbnail(file))
                .size(Files.size(file.toPath()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public byte[] getFileThumbnail(File file) throws IOException {
        String mimeType = Files.probeContentType(file.toPath());

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return MediaThumbnailUtils.getImageThumbnail(file, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            }

            if (mimeType.startsWith("video/")) {
                return MediaThumbnailUtils.getVideoThumbnail(file, THUMBNAIL_SIZE, THUMBNAIL_SIZE,
                        THUMBNAIL_VIDEO_SECONDS);
            }
        }

        return null;
    }
}
