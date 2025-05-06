package app.notesr.cli.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbConnectionTest {
    private static DbConnection dbConnection;

    @BeforeAll
    public static void beforeAll() {
        dbConnection = new DbConnection(":memory:");
    }

    @Test
    public void testConnectionAvailable() throws SQLException {
        assertNotNull(dbConnection.getConnection(), "Connection is null");
        assertTrue(dbConnection.getConnection().isValid(30), "Invalid connection");
        assertFalse(dbConnection.getConnection().isClosed(), "Connection closed");
    }

    @Test
    public void testTablesExists() throws SQLException {
        List<String> tables = List.of("notes", "files_info", "data_blocks");

        for (String table : tables) {
            assertTrue(isTableExists(table), "Table " + table + " wasn't created");
        }
    }

    private boolean isTableExists(String name) throws SQLException {
        Connection connection = dbConnection.getConnection();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
