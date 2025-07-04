package app.notesr.cli.parser.v1;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.NotesJsonParser;
import app.notesr.cli.parser.Parser;
import com.fasterxml.jackson.core.JsonParser;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;


public final class ParserV1 extends Parser {
    private final DbConnection db;

    public ParserV1(Path backupPath, Path outputDbPath) {
        super(backupPath, outputDbPath);
        this.db = new DbConnection(outputDbPath.toString());
    }

    @Override
    public void run() {
        try {
            File backupFile = backupPath.toFile();
            JsonParser jsonParser = getJsonParser(backupFile);

            NotesJsonParser notesJsonParser = new NotesJsonParser(db, jsonParser, DATETIME_FORMATTER);
            FilesJsonParser filesJsonParser = new FilesJsonParserV1(db, jsonParser, DATETIME_FORMATTER);

            notesJsonParser.transferToDb();
            filesJsonParser.transferToDb();
        } catch (IOException e) {
            throw new BackupIOException(e);
        } catch (UnableToExecuteStatementException e) {
            throw new BackupDbException(e);
        }
    }
}
