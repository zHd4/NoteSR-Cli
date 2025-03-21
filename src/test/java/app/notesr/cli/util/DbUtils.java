package app.notesr.cli.util;

import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

public class DbUtils {
    public static void insertNote(Connection connection, Note note) {
        String sql = "INSERT INTO notes (id, name, text, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, note.getId());
            stmt.setString(2, note.getName());
            stmt.setString(3, note.getText());
            stmt.setString(4, dateTimeToString(note.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertFileInfo(Connection connection, FileInfo fileInfo) {
        String sql = "INSERT INTO files_info (id, note_id, size, name, created_at, updated_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fileInfo.getId());
            stmt.setString(2, fileInfo.getNoteId());
            stmt.setLong(3, fileInfo.getSize());
            stmt.setString(4, fileInfo.getName());
            stmt.setString(5, dateTimeToString(fileInfo.getCreatedAt()));
            stmt.setString(6, dateTimeToString(fileInfo.getUpdatedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertDataBlock(Connection connection, DataBlock dataBlock) {
        String sql = "INSERT INTO data_blocks (id, file_id, block_order, data)"
                + " VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dataBlock.getId());
            stmt.setString(2, dataBlock.getFileId());
            stmt.setLong(3, dataBlock.getOrder());
            stmt.setBytes(4, dataBlock.getData());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeTableAsJson(Connection conn, String tableName) throws SQLException,
            JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> data = new ArrayList<>();

        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }

                data.add(row);
            }
        }

        return objectMapper.writeValueAsString(data);
    }
}
