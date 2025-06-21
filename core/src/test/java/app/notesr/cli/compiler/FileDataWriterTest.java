package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.notesr.cli.util.ModelGenerator.generateTestDataBlocks;
import static app.notesr.cli.util.ModelGenerator.generateTestFileInfo;
import static app.notesr.cli.util.ModelGenerator.generateTestNote;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileDataWriterTest {
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;
    private static final int TEST_BLOCK_SIZE = 1000;

    private static final Random RANDOM = new Random();

    @TempDir
    private Path tempDir;

    private DataBlockEntityDao dataBlockEntityDao;

    @BeforeEach
    void setUp() {
        dataBlockEntityDao = mock(DataBlockEntityDao.class);
    }

    @Test
    void testWrite() throws IOException {
        FileInfo testFileInfo = generateTestFileInfo(generateTestNote(), RANDOM.nextLong(MIN_FILE_SIZE, MAX_FILE_SIZE));
        Set<DataBlock> testDataBlocks = generateTestDataBlocks(testFileInfo, TEST_BLOCK_SIZE);

        Set<DataBlock> testDataBlocksWithoutData = testDataBlocks.stream()
                .map(dataBlock -> DataBlock.builder()
                        .id(dataBlock.getId())
                        .fileId(dataBlock.getFileId())
                        .blockOrder(dataBlock.getBlockOrder())
                        .build())
                .collect(Collectors.toSet());

        Map<String, DataBlock> testDataBlocksMap = testDataBlocks.stream()
                .collect(Collectors.toMap(DataBlock::getId, Function.identity()));

        when(dataBlockEntityDao.getAllDataBlocksWithoutData()).thenReturn(testDataBlocksWithoutData);
        when(dataBlockEntityDao.getById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return testDataBlocksMap.get(id);
        });

        File outputDir = tempDir.resolve("output").toFile();

        FileDataWriter fileDataWriter = new FileDataWriter(outputDir, dataBlockEntityDao);
        fileDataWriter.write();

        Map<String, byte[]> expected = testDataBlocks.stream()
                .collect(Collectors.toMap(DataBlock::getId, DataBlock::getData));

        Map<String, byte[]> actual = readFilesAsBytes(outputDir.toPath());
        assertFalse(actual.isEmpty(), "The actual map is empty");

        for (String id : expected.keySet()) {
            assertTrue(actual.containsKey(id), "Id " + id + " not found in the actual map");
            assertArrayEquals(expected.get(id), actual.get(id), "Data of " + id + " is different");
        }
    }

    @Test
    void testWriteWhenOutputDirIsFile() {
        File outputDir = mock(File.class);

        when(outputDir.exists()).thenReturn(true);
        when(outputDir.isDirectory()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            FileDataWriter fileDataWriter = new FileDataWriter(outputDir, dataBlockEntityDao);
            fileDataWriter.write();
        }, "An exception was expected but wasn't thrown");
    }

    private static Map<String, byte[]> readFilesAsBytes(Path dirPath) throws IOException {
        Map<String, byte[]> filesBytesMap = new HashMap<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    byte[] bytes = Files.readAllBytes(path);
                    filesBytesMap.put(path.getFileName().toString(), bytes);
                }
            }
        }

        return filesBytesMap;
    }
}
