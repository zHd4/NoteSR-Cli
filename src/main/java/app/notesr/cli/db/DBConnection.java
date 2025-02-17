package app.notesr.cli.db;

import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Objects.requireNonNull;

@Getter
public class DBConnection {
    private static final String INIT_DB_SCRIPT_RES_FILE_NAME = "init_db_struct.sql";

    private final String dbPath;
    private final Connection connection;

    public DBConnection(String dbPath) {
        this.dbPath = dbPath;
        this.connection = connect(dbPath);

        createStructure();
    }

    private void createStructure() {
        try {
            String sql = getInitDbScript();

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute(sql);
            }
        } catch (URISyntaxException | IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getInitDbScript() throws URISyntaxException, IOException {
        URL url = requireNonNull(DBConnection.class.getClassLoader().getResource(INIT_DB_SCRIPT_RES_FILE_NAME));
        return Files.readString(Paths.get(url.toURI()));
    }

    private static Connection connect(String dbPath) {
        try {
            String url = "jdbc:sqlite:" + dbPath;
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new ConnectionException(e);
        }
    }
}
