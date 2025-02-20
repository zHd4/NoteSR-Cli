package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataBlockDaoTest {
    public static final int TEST_BLOCK_SIZE = 1000;

    public static final int MIN_TEST_FILE_SIZE = 1024;
    public static final int MAX_TEST_FILE_SIZE = 1024 * 20;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    private File dbFile;
    private DbConnection dbConnection;

    private DataBlockDao dataBlockDao;

    private Note testNote;
    private FileInfo testFileInfo;
    private LinkedHashSet<DataBlock> testDataBlocks;

    @BeforeEach
    public void beforeEach() {
        String dbPath = getTempPath(randomUUID().toString());

        dbFile = new File(dbPath);
        dbConnection = new DbConnection(dbPath);

        dataBlockDao = new DataBlockDao(dbConnection);

        testNote = getTestNote();
        testFileInfo = getTestFileInfo();
        testDataBlocks = generateRandomDataBlocks(testFileInfo);
    }

    @AfterEach
    public void afterEach() {
        assertTrue(dbFile.delete());
    }

    private Note getTestNote() {
        return Note.builder()
                .id(randomUUID().toString())
                .name(FAKER.text().text(5, 15))
                .text(FAKER.text().text())
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    private FileInfo getTestFileInfo() {
        return FileInfo.builder()
                .id(randomUUID().toString())
                .noteId(testNote.getId())
                .size(RANDOM.nextLong(MIN_TEST_FILE_SIZE, MAX_TEST_FILE_SIZE))
                .name(FAKER.text().text(5, 15))
                .createdAt(truncateDateTime(LocalDateTime.now()))
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    private DataBlock getDataBlock(FileInfo fileInfo, long order, byte[] data) {
        return DataBlock.builder()
                .id(randomUUID().toString())
                .fileId(fileInfo.getId())
                .order(order)
                .data(data)
                .build();
    }

    private LinkedHashSet<DataBlock> generateRandomDataBlocks(FileInfo fileInfo) {
        List<DataBlock> blocks = new ArrayList<>();

        long order = 0;
        long bytesLeft = fileInfo.getSize();

        while (bytesLeft > TEST_BLOCK_SIZE) {
            byte[] data = new byte[TEST_BLOCK_SIZE];
            RANDOM.nextBytes(data);

            blocks.add(getDataBlock(fileInfo, order, data));
            order++;

            bytesLeft -= TEST_BLOCK_SIZE;
        }

        if (bytesLeft > 0) {
            order++;

            byte[] data = new byte[(int) bytesLeft];
            RANDOM.nextBytes(data);

            blocks.add(getDataBlock(fileInfo, order, data));
        }

        return new LinkedHashSet<>(blocks);
    }
}
