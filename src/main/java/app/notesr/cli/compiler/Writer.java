package app.notesr.cli.compiler;

import java.io.IOException;
import java.sql.SQLException;

interface Writer {
    void write() throws IOException, SQLException;
}
