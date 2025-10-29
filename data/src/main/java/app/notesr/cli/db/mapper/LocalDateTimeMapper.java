package app.notesr.cli.db.mapper;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LocalDateTimeMapper implements ColumnMapper<LocalDateTime> {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime map(ResultSet rs, int columnNumber, StatementContext ctx) throws SQLException {
        String value = rs.getString(columnNumber);
        return value != null ? LocalDateTime.parse(value, DATETIME_FORMATTER) : null;
    }
}
