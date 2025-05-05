package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockDao;
import app.notesr.cli.db.dao.FileInfoDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@RequiredArgsConstructor
class FileInfoExporter implements Exporter {
    private final JsonGenerator jsonGenerator;
    private final FileInfoDao fileInfoDao;
    private final DataBlockDao dataBlockDao;
    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void export() throws IOException, SQLException {
        try (jsonGenerator) {
            jsonGenerator.writeStartObject();

            writeFilesInfos(fileInfoDao.getAll());
            writeDataBlocksWithoutData(dataBlockDao.getAllDataBlocksWithoutData());

            jsonGenerator.writeEndObject();
        }
    }

    private void writeFilesInfos(Set<FileInfo> fileInfos) throws IOException {
        jsonGenerator.writeArrayFieldStart("files_info");

        for (FileInfo fileInfo : fileInfos) {
            writeFileInfo(fileInfo);
        }

        jsonGenerator.writeEndArray();
    }

    private void writeDataBlocksWithoutData(Set<DataBlock> dataBlocks) throws IOException {
        jsonGenerator.writeArrayFieldStart("files_data_blocks");

        for (DataBlock dataBlock : dataBlocks) {
            writeDataBlockInfo(dataBlock);
        }

        jsonGenerator.writeEndArray();
    }

    private void writeFileInfo(FileInfo fileInfo) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", fileInfo.getId());
        jsonGenerator.writeStringField("note_id", fileInfo.getNoteId());
        jsonGenerator.writeNumberField("size", fileInfo.getSize());

        jsonGenerator.writeStringField("name", fileInfo.getName());
        jsonGenerator.writeStringField("type", fileInfo.getType());

        if (fileInfo.getThumbnail() != null) {
            jsonGenerator.writeBinaryField("thumbnail", fileInfo.getThumbnail());
        }

        String createdAt = fileInfo.getCreatedAt().format(dateTimeFormatter);
        String updatedAt = fileInfo.getUpdatedAt().format(dateTimeFormatter);

        jsonGenerator.writeStringField("created_at", createdAt);
        jsonGenerator.writeStringField("updated_at", updatedAt);

        jsonGenerator.writeEndObject();
    }

    private void writeDataBlockInfo(DataBlock dataBlock) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("id", dataBlock.getId());
        jsonGenerator.writeStringField("file_id", dataBlock.getFileId());
        jsonGenerator.writeNumberField("order", dataBlock.getOrder());

        jsonGenerator.writeEndObject();
    }
}
