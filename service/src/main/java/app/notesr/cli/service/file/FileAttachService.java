package app.notesr.cli.service.file;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.core.exception.NoteNotFoundException;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import app.notesr.cli.core.util.MediaThumbnailUtils;
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
        Note note = getNote(noteId);

        if (note == null) {
            throw new NoteNotFoundException(noteId);
        }

        FileInfo fileInfo = buildFileInfo(file, noteId);

        db.getConnection().useTransaction(handle -> {
            NoteEntityDao noteEntityDao = handle.attach(NoteEntityDao.class);
            FileInfoEntityDao fileInfoEntityDao = handle.attach(FileInfoEntityDao.class);

            fileInfoEntityDao.add(fileInfo);

            DataBlockEntityDao dataBlockDao = handle.attach(DataBlockEntityDao.class);
            ChunkedFileUploader fileUploader = new ChunkedFileUploader(dataBlockDao);
            fileUploader.upload(fileInfo.getId(), file);

            note.setUpdatedAt(LocalDateTime.now());
            noteEntityDao.update(note);
        });
    }

    private Note getNote(String noteId) {
        NoteEntityDao dao = db.getConnection().onDemand(NoteEntityDao.class);
        return dao.getById(noteId);
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

    private byte[] getFileThumbnail(File file) throws IOException {
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
