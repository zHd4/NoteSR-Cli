package app.notesr.cli.parser.v2;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.parser.BackupIOException;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.NotesJsonParser;
import app.notesr.cli.parser.Parser;
import app.notesr.cli.util.ZipUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public final class ParserV2 extends Parser {
    private static final String NOTES_JSON_FILE_NAME = "notes.json";
    private static final String FILES_INFO_JSON_FILE_NAME = "files_info.json";
    private static final String DATA_BLOCKS_DIR_NAME = "data_blocks";

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path tempDirPath;
    private final DbConnection db;

    public ParserV2(Path backupPath, Path tempDirPath, Path outputDbPath) {
        super(backupPath, outputDbPath);

        this.tempDirPath = tempDirPath;
        this.db = new DbConnection(outputDbPath.toString());
    }

    @Override
    public void run() {
        try {
            ZipUtils.unzip(backupPath.toString(), tempDirPath.toString());

            File notesJsonFile = new File(tempDirPath.toString(), NOTES_JSON_FILE_NAME);
            File filesInfosJsonFile = new File(tempDirPath.toString(), FILES_INFO_JSON_FILE_NAME);

            NotesJsonParser notesJsonParser = new NotesJsonParser(db, getJsonParser(notesJsonFile), DATETIME_FORMATTER);
            FilesJsonParser filesJsonParser =
                    new FilesJsonParserV2(db, getJsonParser(filesInfosJsonFile), DATETIME_FORMATTER);

            notesJsonParser.transferToDb();
            filesJsonParser.transferToDb();
        } catch (IOException e) {
            throw new BackupIOException(e);
        }
    }

    private JsonParser getJsonParser(File jsonFile) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(jsonFile);
    }
}
