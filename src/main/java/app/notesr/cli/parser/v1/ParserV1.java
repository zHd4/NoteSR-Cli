package app.notesr.cli.parser.v1;

import app.notesr.cli.parser.Parser;

import java.nio.file.Path;

public class ParserV1 extends Parser {
    public ParserV1(Path backupPath, Path outputDbPath) {
        super(backupPath, outputDbPath);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
