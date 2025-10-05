package app.notesr.cli.db;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LocalDateTimeArgumentFactory extends AbstractArgumentFactory<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    LocalDateTimeArgumentFactory() {
        super(Types.VARCHAR);
    }

    @Override
    protected Argument build(LocalDateTime value, ConfigRegistry config) {
        return (position, statement, ctx) ->
                statement.setString(position, value.format(FORMATTER));
    }
}
