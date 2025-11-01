package app.notesr.cli.parser;

import app.notesr.cli.core.exception.BackupDbException;
import app.notesr.cli.core.exception.BackupIOException;

public interface Parser {
    void parse() throws BackupParserException, BackupDbException, BackupIOException;
}
