package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockDao;
import app.notesr.cli.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
class FileDataJsonWriter implements JsonWriter {
    private final File outputDir;
    private final DataBlockDao dataBlockDao;

    @Override
    public void write() throws IOException, SQLException {
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                throw new IllegalArgumentException(outputDir.getAbsolutePath() + " isn't directory");
            }
        } else {
            if (!outputDir.mkdir()) {
                throw new IOException("Failed to create temporary directory " + outputDir.getAbsolutePath());
            }
        }

        for (DataBlock dataBlock : dataBlockDao.getAllDataBlocksWithoutData()) {
            File blockDataFile = new File(outputDir, dataBlock.getId());
            byte[] blockData = requireNonNull(dataBlockDao.getById(dataBlock.getId())).getData();

            Files.write(blockDataFile.toPath(), blockData);
        }
    }
}
