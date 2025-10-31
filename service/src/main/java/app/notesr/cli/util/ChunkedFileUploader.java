package app.notesr.cli.util;

import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.util.UUID.randomUUID;

@RequiredArgsConstructor
public final class ChunkedFileUploader {
    static final int CHUNK_SIZE = 500_000;
    private final DataBlockEntityDao dao;

    public void upload(String fileId, File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file)) {
            byte[] chunk = new byte[CHUNK_SIZE];
            long order = 0;
            int bytesRead;

            while ((bytesRead = stream.read(chunk)) != -1) {
                byte[] data = bytesRead == CHUNK_SIZE ? chunk : Arrays.copyOf(chunk, bytesRead);
                DataBlock dataBlock = DataBlock.builder()
                        .id(randomUUID().toString())
                        .fileId(fileId)
                        .blockOrder(order++)
                        .data(data)
                        .build();
                dao.add(dataBlock);
            }
        }
    }
}
