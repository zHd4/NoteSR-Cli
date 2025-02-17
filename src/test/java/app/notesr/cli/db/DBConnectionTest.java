package app.notesr.cli.db;

import app.notesr.cli.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.SQLException;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DBConnectionTest extends TestBase {

    private static String dbPath;
    private static DBConnection dbConnection;

    @BeforeAll
    public static void beforeAll() {
        dbPath = getTempPath(randomUUID().toString());
        dbConnection = new DBConnection(dbPath);
    }

    @Test
    public void testDbExists() {
        File dbFile = new File(dbPath);
        assertTrue(dbFile.exists(), dbFile.getAbsolutePath() + " not found");
    }

    @Test
    public void testConnectionAvailable() throws SQLException {
        assertNotNull(dbConnection.getConnection(), "Connection is null");
        assertTrue(dbConnection.getConnection().isValid(30), "Invalid connection");
        assertFalse(dbConnection.getConnection().isClosed(), "Connection closed");
    }
}
