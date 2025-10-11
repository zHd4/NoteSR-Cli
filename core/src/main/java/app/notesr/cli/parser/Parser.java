package app.notesr.cli.parser;

import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;

public interface Parser{
    void parse() throws BackupParserException, BackupDbException, BackupIOException;
}
