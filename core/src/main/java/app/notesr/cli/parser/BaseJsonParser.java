package app.notesr.cli.parser;

import com.fasterxml.jackson.core.JsonParser;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public abstract class BaseJsonParser {
    protected final JsonParser parser;
    protected final DateTimeFormatter timestampFormatter;

    protected final boolean skipTo(String targetField) throws IOException {
        String currentField = parser.getCurrentName();

        while (currentField == null || !currentField.equals(targetField)) {
            if (parser.nextToken() == null) {
                return false;
            }

            currentField = parser.getCurrentName();
        }

        return true;
    }

    public abstract void transferToDb() throws IOException;
}
