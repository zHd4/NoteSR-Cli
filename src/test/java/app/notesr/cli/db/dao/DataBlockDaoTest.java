package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static app.notesr.cli.util.ModelGenerator.generateTestDataBlocks;
import static app.notesr.cli.util.ModelGenerator.generateTestFileInfo;
import static app.notesr.cli.util.ModelGenerator.generateTestNote;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataBlockDaoTest {
    public static final int TEST_BLOCK_SIZE = 1000;

    public static final int MIN_TEST_FILE_SIZE = 1024;
    public static final int MAX_TEST_FILE_SIZE = 1024 * 20;

    private static final Random RANDOM = new Random();

    private DbConnection db;
    private DataBlockDao dataBlockDao;

    private long testFileSize;

    private Note testNote;
    private FileInfo testFileInfo;
    private Set<DataBlock> testDataBlocks;

    @BeforeEach
    public void beforeEach() {
        db = new DbConnection(":memory:");
        dataBlockDao = new DataBlockDao(db);

        testFileSize = RANDOM.nextLong(MIN_TEST_FILE_SIZE, MAX_TEST_FILE_SIZE);

        testNote = generateTestNote();
        testFileInfo = generateTestFileInfo(testNote, testFileSize);
        testDataBlocks = generateTestDataBlocks(testFileInfo, TEST_BLOCK_SIZE);

        DbUtils.insertNote(db.getConnection(), testNote);
        DbUtils.insertFileInfo(db.getConnection(), testFileInfo);
    }

    @Test
    public void testAdd() throws SQLException {
        for (DataBlock testDataBlock : testDataBlocks) {
            dataBlockDao.add(testDataBlock);
        }

        Set<DataBlock> actual = new LinkedHashSet<>();
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

                actual.add(dataBlock);
            }
        }

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

        FileInfo additionalTestFileInfo = generateTestFileInfo(testNote, testFileSize);
        DbUtils.insertFileInfo(db.getConnection(), additionalTestFileInfo);

        Set<DataBlock> additionalTestDataBlocks = generateTestDataBlocks(additionalTestFileInfo, TEST_BLOCK_SIZE);
        additionalTestDataBlocks.forEach(dataBlock ->
                DbUtils.insertDataBlock(db.getConnection(), dataBlock));

        Set<String> expected = testDataBlocks.stream().map(DataBlock::getId).collect(Collectors.toSet());
        Set<String> actual = dataBlockDao.getIdsByFileId(testFileInfo.getId());

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
}
