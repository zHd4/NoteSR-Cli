package app.notesr.cli.parser;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class FilesJsonParser extends BaseJsonParser {
    private static final String ROOT_NAME = "files_info";

    protected final FileInfoEntityDao fileInfoEntityDao;
    protected final DataBlockEntityDao dataBlockEntityDao;

    public FilesJsonParser(DbConnection db, JsonParser parser, DateTimeFormatter timestampFormatter) {
        super(parser, timestampFormatter);
        this.fileInfoEntityDao = new FileInfoEntityDao(db);
        this.dataBlockEntityDao = new DataBlockEntityDao(db);
    }

    @Override
    public final void transferToDb() throws IOException, SQLException {
        transferFilesInfo();
        transferFilesData();
    }

    protected final void transferFilesInfo() {
        try {
            if (!skipTo(ROOT_NAME)) {
                throw new BackupParserException("'" + ROOT_NAME + "' field not found in json");
            }

            if (parser.nextToken() == JsonToken.START_ARRAY) {
                do {
                    FileInfo fileInfo = new FileInfo();
                    transferFileInfoObject(fileInfo);

                    if (fileInfo.getId() != null) {
                        fileInfoEntityDao.add(fileInfo);
                    }
                } while (parser.nextToken() != JsonToken.END_ARRAY);
            }
        } catch (IOException e) {
            throw new BackupIOException(e);
        } catch (SQLException e) {
            throw new BackupDbException(e);
        }
    }

    protected final void transferFileInfoObject(FileInfo fileInfo) throws IOException {
        String field;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            field = parser.getCurrentName();

            if (field != null) {
                switch (field) {
                    case "id" -> {
                        if (parser.getValueAsString().equals("id")) {
                            continue;
                        }

                        fileInfo.setId(parser.getValueAsString());
                    }

                    case "note_id" -> {
                        if (parser.getValueAsString().equals("note_id")) {
                            continue;
                        }

                        fileInfo.setNoteId(parser.getValueAsString());
                    }

                    case "size" -> {
                        if (parser.getValueAsString().equals("size")) {
                            continue;
                        }

                        fileInfo.setSize(parser.getValueAsLong());
                    }

                    case "name" -> {
                        if (parser.getValueAsString().equals("name")) {
                            continue;
                        }
                        fileInfo.setName(parser.getValueAsString());
                    }

                    case "type" -> {
                        String value = parser.getValueAsString();

                        if (value != null) {
                            if (value.equals("type")) {
                                continue;
                            }

                            fileInfo.setType(parser.getValueAsString());
                        }
                    }

                    case "thumbnail" -> {
                        String value = parser.getValueAsString();

                        if (value != null) {
                            if (value.equals("thumbnail")) {
                                continue;
                            }

                            fileInfo.setThumbnail(parser.getBinaryValue());
                        }
                    }

                    case "created_at" -> {
                        if (parser.getValueAsString().equals("created_at")) {
                            continue;
                        }

                        fileInfo.setCreatedAt(
                                LocalDateTime.parse(parser.getValueAsString(), timestampFormatter)
                        );
                    }

                    case "updated_at" -> {
                        if (parser.getValueAsString().equals("updated_at")) {
                            continue;
                        }

                        fileInfo.setUpdatedAt(
                                LocalDateTime.parse(parser.getValueAsString(), timestampFormatter)
                        );
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

    protected abstract void transferFilesData() throws IOException, SQLException;

    protected abstract void parseDataBlockObject(DataBlock dataBlock) throws IOException;
}
