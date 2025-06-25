package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
public final class FileExtractionService {
    private final DbConnection db;

    public FileInfo getFileInfo(String fileId) {
        FileInfoEntityDao fileInfoDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        return fileInfoDao.getById(fileId);
    }

    public void extractFile(String fileId, File outputFile) throws IOException {
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);

            for (String id : dataBlockEntityDao.getIdsByFileId(fileId)) {
                DataBlock dataBlock = dataBlockEntityDao.getById(id);

                if (dataBlock == null || dataBlock.getData() == null) {
                    throw new NullPointerException("Data block with id " + id
                            + " not found, possible database corrupted");
                }

                output.write(dataBlock.getData());
            }
        }
    }
}
