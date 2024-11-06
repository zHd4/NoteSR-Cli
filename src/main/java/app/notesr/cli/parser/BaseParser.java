package app.notesr.cli.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;

abstract class BaseParser implements Parser {
    protected JsonParser parser;

    public BaseParser(InputStream stream) {
        try {
            JsonFactory factory = new JsonFactory();
            this.parser = factory.createParser(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
