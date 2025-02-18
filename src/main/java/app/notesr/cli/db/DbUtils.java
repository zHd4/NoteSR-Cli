package app.notesr.cli.db;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DbUtils {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String dateTimeToString(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    public static LocalDateTime truncateDateTime(LocalDateTime dateTime) {
        return LocalDateTime.parse(dateTime.format(DATETIME_FORMATTER), DATETIME_FORMATTER);
    }
}
