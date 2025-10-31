package app.notesr.cli.data;

import app.notesr.cli.data.mapper.LocalDateTimeMapper;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToCreateStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Getter
public class DbConnection {
    private static final String INIT_DB_SCRIPT_RES_PATH = "/init_db_struct.sql";

    private final String subname;
    private final Jdbi connection;

    public DbConnection(String subname) {
        this.subname = subname;
        this.connection = connect(subname);

        createStructure();
    }

    private void createStructure() {
        try (InputStream scriptStream = requireNonNull(getClass().getResourceAsStream(INIT_DB_SCRIPT_RES_PATH));
             BufferedReader reader = new BufferedReader(new InputStreamReader(scriptStream))) {

            connection.useHandle(handle -> {
                handle.execute("PRAGMA foreign_keys = ON;");
                String sql = reader.lines().collect(Collectors.joining("\n"));

                for (String statement : sql.split("(?m);\\s*$")) {
                    if (!statement.trim().isEmpty()) {
                        handle.execute(statement);
                    }
                }
            });
        } catch (UnableToCreateStatementException e) {
            if (e.getMessage().contains("SQLITE_NOTADB")) {
                throw new InvalidDbException(subname + ": invalid database", e);
            }

            throw new ConnectionException(e);
        } catch (IOException | RuntimeException e) {
            throw new ConnectionException(e);
        }
    }

    private static Jdbi connect(String subname) {
        try {
            String url = "jdbc:sqlite:" + subname;
            Jdbi jdbi = Jdbi.create(url);

            jdbi.installPlugin(new SqlObjectPlugin());
            jdbi.registerColumnMapper(LocalDateTime.class, new LocalDateTimeMapper());
            jdbi.registerArgument(new LocalDateTimeArgumentFactory());

            return jdbi;
        } catch (RuntimeException e) {
            throw new ConnectionException(e);
        }
    }
}
