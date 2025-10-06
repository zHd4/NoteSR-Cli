package app.notesr.cli.parser.v2;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.UnexpectedFieldException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

final class FilesJsonParserV2 extends FilesJsonParser {
    private static final String ROOT_NAME = "files_data_blocks";
    private static final String DATA_BLOCKS_DIR_NAME = "data_blocks";

    private final Path tempDirPath;

    FilesJsonParserV2(DbConnection db, JsonParser parser, Path tempDirPath,
                             DateTimeFormatter timestampFormatter) {
        super(db, parser, timestampFormatter);
        this.tempDirPath = tempDirPath;
    }

    @Override
    protected void transferFilesData() throws IOException {
        if (skipTo(ROOT_NAME)) {
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                do {
                    DataBlock dataBlock = new DataBlock();
                    parseDataBlockObject(dataBlock);

                    String id = dataBlock.getId();

                    if (id != null) {
                        dataBlock.setData(readDataBlock(id));
                        getDataBlockEntityDao().add(dataBlock);
                    }
                } while (parser.nextToken() != JsonToken.END_ARRAY);
            }
        }
    }

    @Override
    protected void parseDataBlockObject(DataBlock dataBlock) throws IOException {
        String field;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            field = parser.getCurrentName();

            if (field != null) {
                switch (field) {
                    case "id" -> {
                        if (parser.getValueAsString().equals("id")) {
                            continue;
                        }

                        dataBlock.setId(parser.getValueAsString());
                    }

                    case "file_id" -> {
                        if (parser.getValueAsString().equals("file_id")) {
                            continue;
                        }

                        dataBlock.setFileId(parser.getValueAsString());
                    }

                    case "order" -> {
                        if (parser.getValueAsString().equals("order")) {
                            continue;
                        }

                        dataBlock.setBlockOrder(parser.getValueAsLong());
                    }

                    default -> {
                        if (!field.equals(ROOT_NAME)) {
                            throw new UnexpectedFieldException("Unexpected field: " + field);
                        }
                    }
                }
            }
        }
    }

    private byte[] readDataBlock(String id) throws IOException {
        return Files.readAllBytes(Path.of(tempDirPath.toString(), DATA_BLOCKS_DIR_NAME, id));
    }
}
