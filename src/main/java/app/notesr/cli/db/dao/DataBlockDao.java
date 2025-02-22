package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public Set<String> getIdsByFileId(String fileId) throws SQLException {
        LinkedHashSet<String> results = new LinkedHashSet<>();

        String sql = "SELECT id FROM data_blocks WHERE file_id = ? ORDER BY block_order";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, fileId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(rs.getString(1));
            }
        }

        return results;
    }

    public DataBlock getById(String id) throws SQLException {
        String sql = "SELECT * FROM data_blocks WHERE id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return DataBlock.builder()
                        .id(rs.getString(1))
                        .fileId(rs.getString(2))
                        .order(rs.getLong(3))
                        .data(rs.getBytes(4))
                        .build();
            }
        }

        return null;
    }
}
