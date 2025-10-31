package app.notesr.cli.util;


import app.notesr.cli.data.dao.DataBlockEntityDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static app.notesr.cli.util.ChunkedFileUploader.CHUNK_SIZE;
import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ChunkedFileUploaderTest {
    private static final Random RANDOM = new Random();

    @TempDir
    private Path tempDir;

    @Test
    void testUpload() throws IOException {
        byte[] testFileData = getRandomData();
        File testFile = createTestFile(testFileData);
        String testFileId = randomUUID().toString();
        DataBlockEntityDao testDataBlockEntityDao = mock(DataBlockEntityDao.class);

        ChunkedFileUploader uploader = new ChunkedFileUploader(testDataBlockEntityDao);
        uploader.upload(testFileId, testFile);

        int expectedBlocksCount = (int) Math.ceil((double) testFileData.length / CHUNK_SIZE);
        verify(testDataBlockEntityDao, times(expectedBlocksCount)).add(any());
    }

    private File createTestFile(byte[] testFileData) throws IOException {
        File testFile = tempDir.resolve("test_file").toFile();
        Files.write(testFile.toPath(), testFileData);

        return testFile;
    }

    private byte[] getRandomData() {
        int size = RANDOM.nextInt(CHUNK_SIZE / 2, CHUNK_SIZE * 4);
        byte[] data = new byte[size];

        RANDOM.nextBytes(data);
        return data;
    }
}
