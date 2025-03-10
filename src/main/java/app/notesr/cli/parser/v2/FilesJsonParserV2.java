package app.notesr.cli.parser.v2;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.parser.FilesJsonParser;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public final class FilesJsonParserV2 extends FilesJsonParser {

    public FilesJsonParserV2(DbConnection db, JsonParser parser, DateTimeFormatter timestampFormatter) {
        super(db, parser, timestampFormatter);
    }

    @Override
    protected void transferFilesData() throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected void parseDataBlockObject(DataBlock dataBlock) throws IOException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
