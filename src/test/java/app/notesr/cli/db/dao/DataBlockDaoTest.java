package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.FixtureUtils;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class DataBlockDaoTest {
    public static final int TEST_BLOCK_SIZE = 1000;

    public static final int MIN_TEST_FILE_SIZE = 1024;
    public static final int MAX_TEST_FILE_SIZE = 1024 * 20;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    private DbConnection dbConnection;
    private DataBlockDao dataBlockDao;

    private Note testNote;
    private FileInfo testFileInfo;
    private LinkedHashSet<DataBlock> testDataBlocks;

    @BeforeEach
    public void beforeEach() {
        dbConnection = new DbConnection(":memory:");
        dataBlockDao = new DataBlockDao(dbConnection);

        testNote = getTestNote();
        testFileInfo = getTestFileInfo();
        testDataBlocks = generateRandomDataBlocks(testFileInfo);
    }

    @Test
    public void testAdd() throws SQLException {
        FixtureUtils.insertNote(dbConnection.getConnection(), testNote);
        FixtureUtils.insertFileInfo(dbConnection.getConnection(), testFileInfo);

        for (DataBlock testDataBlock : testDataBlocks) {
            dataBlockDao.add(testDataBlock);
        }

        List<DataBlock> actualList = new ArrayList<>();

        String sql = "SELECT * FROM data_blocks WHERE file_id = ?";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, testFileInfo.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                DataBlock dataBlock = DataBlock.builder()
                        .id(rs.getString(1))
                        .fileId(rs.getString(2))
                        .order(rs.getLong(3))
                        .data(rs.getBytes(4))
                        .build();

                actualList.add(dataBlock);
            }
        }

        LinkedHashSet<DataBlock> actual = new LinkedHashSet<>(actualList);

        assertFalse(actual.isEmpty(), "Actual must be not empty");
        assertEquals(testDataBlocks, actual, "Data blocks are different");
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
