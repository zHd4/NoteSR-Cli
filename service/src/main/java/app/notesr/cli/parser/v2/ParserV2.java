package app.notesr.cli.parser.v2;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.NotesJsonParser;
import app.notesr.cli.parser.Parser;
import app.notesr.cli.util.ZipUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
public final class ParserV2 implements Parser {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String NOTES_JSON_FILE_NAME = "notes.json";
    private static final String FILES_INFO_JSON_FILE_NAME = "files_info.json";

    private final Path backupPath;
    private final Path tempDirPath;
    private final Path outputDbPath;

    @Override
    public void parse() {
        try {
            ZipUtils.unzip(backupPath.toString(), tempDirPath.toString());

            File notesJsonFile = new File(tempDirPath.toString(), NOTES_JSON_FILE_NAME);
            File filesInfosJsonFile = new File(tempDirPath.toString(), FILES_INFO_JSON_FILE_NAME);

            DbConnection db = new DbConnection(outputDbPath.toString());

            NotesJsonParser notesJsonParser =
                    new NotesJsonParser(db, getJsonParser(notesJsonFile), DATETIME_FORMATTER);

            FilesJsonParser filesJsonParser =
                    new FilesJsonParserV2(db, getJsonParser(filesInfosJsonFile), tempDirPath, DATETIME_FORMATTER);

            notesJsonParser.transferToDb();
            filesJsonParser.transferToDb();
        } catch (IOException e) {
            throw new BackupIOException(e);
        } catch (UnableToExecuteStatementException e) {
            throw new BackupDbException(e);
        }
    }

    private JsonParser getJsonParser(File jsonFile) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(jsonFile);
    }
}
