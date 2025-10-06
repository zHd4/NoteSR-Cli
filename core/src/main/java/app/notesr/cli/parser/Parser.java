package app.notesr.cli.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class Parser implements Runnable {
    protected static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path backupPath;
    private final Path outputDbPath;

    protected final JsonParser getJsonParser(File jsonFile) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(jsonFile);
    }
}
