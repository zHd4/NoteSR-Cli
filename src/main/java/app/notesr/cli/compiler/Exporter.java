package app.notesr.cli.compiler;

import java.io.IOException;
import java.sql.SQLException;

interface Exporter {
    void export() throws IOException, SQLException;
}
