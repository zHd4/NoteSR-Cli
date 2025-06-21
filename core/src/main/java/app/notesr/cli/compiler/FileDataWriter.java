package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
class FileDataWriter implements Writer {
    private final File outputDir;
    private final DataBlockEntityDao dataBlockEntityDao;

    @Override
    public void write() throws IOException {
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                throw new IllegalArgumentException(outputDir.getAbsolutePath() + " isn't directory");
            }
        } else {
            Files.createDirectory(outputDir.toPath());
        }

        for (DataBlock dataBlock : dataBlockEntityDao.getAllDataBlocksWithoutData()) {
            File blockDataFile = new File(outputDir, dataBlock.getId());
            byte[] blockData = requireNonNull(dataBlockEntityDao.getById(dataBlock.getId())).getData();

            Files.write(blockDataFile.toPath(), blockData);
        }
    }
}
