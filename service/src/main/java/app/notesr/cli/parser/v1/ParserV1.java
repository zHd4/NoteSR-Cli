package app.notesr.cli.parser.v1;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.core.exception.BackupDbException;
import app.notesr.cli.core.exception.BackupIOException;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.NotesJsonParser;
import app.notesr.cli.parser.Parser;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public final class ParserV1 implements Parser {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path backupPath;
    private final Path outputDbPath;

    @Override
    public void parse() {
        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(backupPath.toFile());

            DbConnection db = new DbConnection(outputDbPath.toString());

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
