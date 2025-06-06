package app.notesr.cli.parser.v2;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.NotesJsonParser;
import app.notesr.cli.parser.Parser;
import app.notesr.cli.util.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

public final class ParserV2 extends Parser {
    private static final String NOTES_JSON_FILE_NAME = "notes.json";
    private static final String FILES_INFO_JSON_FILE_NAME = "files_info.json";

    private final Path tempDirPath;

    public ParserV2(Path backupPath, Path tempDirPath, Path outputDbPath) {
        super(backupPath, outputDbPath);
        this.tempDirPath = tempDirPath;
    }

    @Override
    public void run() {
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
        } catch (SQLException e) {
            throw new BackupDbException(e);
        }
    }
}
