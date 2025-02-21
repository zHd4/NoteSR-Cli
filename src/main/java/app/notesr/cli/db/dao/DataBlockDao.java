package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public final class DataBlockDao {
    private final DbConnection db;

    public void add(DataBlock dataBlock) throws SQLException {
        String sql = "INSERT INTO data_blocks (id, file_id, block_order, data) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, dataBlock.getId());
            stmt.setString(2, dataBlock.getFileId());
            stmt.setLong(3, dataBlock.getOrder());
            stmt.setBytes(4, dataBlock.getData());

            stmt.executeUpdate();
        }
    }
}
