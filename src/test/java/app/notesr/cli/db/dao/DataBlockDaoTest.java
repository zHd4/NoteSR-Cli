package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
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
import java.util.Set;
import java.util.stream.Collectors;

import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataBlockDaoTest {
    public static final int TEST_BLOCK_SIZE = 1000;

    public static final int MIN_TEST_FILE_SIZE = 1024;
    public static final int MAX_TEST_FILE_SIZE = 1024 * 20;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    private DbConnection db;
    private DataBlockDao dataBlockDao;

    private Note testNote;
    private FileInfo testFileInfo;
    private LinkedHashSet<DataBlock> testDataBlocks;

    @BeforeEach
    public void beforeEach() {
        db = new DbConnection(":memory:");
        dataBlockDao = new DataBlockDao(db);

        testNote = getTestNote();
        testFileInfo = getTestFileInfo();
        testDataBlocks = generateRandomDataBlocks(testFileInfo);

        DbUtils.insertNote(db.getConnection(), testNote);
        DbUtils.insertFileInfo(db.getConnection(), testFileInfo);
    }

    @Test
    public void testAdd() throws SQLException {
        for (DataBlock testDataBlock : testDataBlocks) {
            dataBlockDao.add(testDataBlock);
        }

        List<DataBlock> actualList = new ArrayList<>();

        String sql = "SELECT * FROM data_blocks WHERE file_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
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

    @Test
    public void testGetAllDataBlocksWithoutData() throws SQLException {
        testDataBlocks.forEach(dataBlock -> DbUtils.insertDataBlock(db.getConnection(), dataBlock));

        Set<DataBlock> expected = testDataBlocks.stream()
                .peek(dataBlock -> dataBlock.setData(null))
                .collect(Collectors.toSet());

        Set<DataBlock> actual = dataBlockDao.getAllDataBlocksWithoutData();

        assertNotNull(actual, "Actual data blocks must be not null");
        assertEquals(expected, actual, "Data blocks are different");
    }

    @Test
    public void testGetIdsByFileId() throws SQLException {
        testDataBlocks.forEach(dataBlock -> DbUtils.insertDataBlock(db.getConnection(), dataBlock));

        FileInfo additionalTestFileInfo = getTestFileInfo();
        DbUtils.insertFileInfo(db.getConnection(), additionalTestFileInfo);

        LinkedHashSet<DataBlock> additionalTestDataBlocks = generateRandomDataBlocks(additionalTestFileInfo);
        additionalTestDataBlocks.forEach(dataBlock ->
                DbUtils.insertDataBlock(db.getConnection(), dataBlock));

        List<String> expected = testDataBlocks.stream().map(DataBlock::getId).collect(Collectors.toList());
        List<String> actual = new ArrayList<>(dataBlockDao.getIdsByFileId(testFileInfo.getId()));

        assertFalse(actual.isEmpty(), "Actual data blocks ids must be not empty");
        assertEquals(expected, actual, "Data blocks ids are different");
    }

    @Test
    public void testGetById() throws SQLException {
        for (DataBlock expected : testDataBlocks) {
            DbUtils.insertDataBlock(db.getConnection(), expected);
            DataBlock actual = dataBlockDao.getById(expected.getId());

            assertNotNull(actual, "Actual data block must be not null");
            assertEquals(expected, actual, "Data blocks are different");
        }
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
