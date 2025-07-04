package app.notesr.cli.service;

import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.BackupParser;
import app.notesr.cli.parser.BackupParserException;
import app.notesr.cli.parser.UnexpectedFieldException;

import java.io.File;
import java.nio.file.Path;

public final class BackupParsingService {
    public Path parse(File decryptedBackup, File outputFile)
            throws BackupIOException, BackupParserException, UnexpectedFieldException, BackupDbException {

        BackupParser parser = new BackupParser(decryptedBackup.toPath(), outputFile.toPath());
        parser.run();

        return parser.getTempDirPath();
    }
}
