package app.notesr.cli.db;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Objects.requireNonNull;

@Getter
public class DbConnection {
    private static final String INIT_DB_SCRIPT_RES_PATH = "/init_db_struct.sql";

    private final String dbPath;
    private final Connection connection;

    public DbConnection(String dbPath) {
        this.dbPath = dbPath;
        this.connection = connect(dbPath);

        createStructure();
    }

    private void createStructure() {
        try {
            InputStream scriptStream =
                    requireNonNull(DbConnection.class.getResourceAsStream(INIT_DB_SCRIPT_RES_PATH));

            try (Statement stmt = connection.createStatement();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(scriptStream))) {
                stmt.execute("PRAGMA foreign_keys = ON;");

                StringBuilder sql = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sql.append(line).append("\n");

                    if (line.trim().endsWith(";")) {
                        stmt.executeUpdate(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
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
