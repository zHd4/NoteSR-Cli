package app.notesr.cli.db;

import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class LocalDateTimeArgumentFactory implements ArgumentFactory {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Optional<Argument> build(Type type, Object value, ConfigRegistry config) {
        if (value instanceof LocalDateTime localDateTime) {
            return Optional.of((position, statement, ctx) ->
                    statement.setString(position, localDateTime.format(DATETIME_FORMATTER)));
        }

        return Optional.empty();
    }
}
