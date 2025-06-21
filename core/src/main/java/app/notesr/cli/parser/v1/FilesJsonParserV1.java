package app.notesr.cli.parser.v1;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.parser.FilesJsonParser;
import app.notesr.cli.parser.UnexpectedFieldException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

final class FilesJsonParserV1 extends FilesJsonParser {
    private static final String ROOT_NAME = "files_data_blocks";

    FilesJsonParserV1(DbConnection db, JsonParser parser, DateTimeFormatter timestampFormatter) {
        super(db, parser, timestampFormatter);
    }

    @Override
    protected void transferFilesData() throws IOException {
        if (skipTo(ROOT_NAME)) {
            if (parser.nextToken() == JsonToken.START_ARRAY) {
                do {
                    DataBlock dataBlock = new DataBlock();
                    parseDataBlockObject(dataBlock);

                    if (dataBlock.getId() != null) {
                        dataBlockEntityDao.add(dataBlock);
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

                    case "data" -> {
                        if (!parser.getValueAsString().equals("data")) {
                            byte[] data = parser.getBinaryValue();
                            dataBlock.setData(data);
                        }
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
}
