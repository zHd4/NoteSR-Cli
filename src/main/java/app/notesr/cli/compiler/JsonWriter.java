package app.notesr.cli.compiler;

import java.io.IOException;
import java.sql.SQLException;

interface JsonWriter {
    void write() throws IOException, SQLException;
}
