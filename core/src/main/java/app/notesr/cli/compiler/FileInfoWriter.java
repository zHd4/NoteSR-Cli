package app.notesr.cli.compiler;

import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@RequiredArgsConstructor
class FileInfoWriter implements Writer {
    static final String FILES_INFOS_ARRAY_NAME = "files_info";
    static final String FILES_DATA_BLOCKS_ARRAY_NAME = "files_data_blocks";

    private final JsonGenerator jsonGenerator;
    private final FileInfoEntityDao fileInfoEntityDao;
    private final DataBlockEntityDao dataBlockEntityDao;
    private final DateTimeFormatter dateTimeFormatter;

    @Override
    public void write() throws IOException {
        try (jsonGenerator) {
            jsonGenerator.writeStartObject();

            writeFilesInfos(fileInfoEntityDao.getAll());
            writeDataBlocksWithoutData(dataBlockEntityDao.getAllDataBlocksWithoutData());

            jsonGenerator.writeEndObject();
        }
    }

    private void writeFilesInfos(Set<FileInfo> fileInfos) throws IOException {
        jsonGenerator.writeArrayFieldStart(FILES_INFOS_ARRAY_NAME);

        for (FileInfo fileInfo : fileInfos) {
            writeFileInfo(fileInfo);
        }

        jsonGenerator.writeEndArray();
    }

    private void writeDataBlocksWithoutData(Set<DataBlock> dataBlocks) throws IOException {
        jsonGenerator.writeArrayFieldStart(FILES_DATA_BLOCKS_ARRAY_NAME);

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
        jsonGenerator.writeNumberField("order", dataBlock.getBlockOrder());

        jsonGenerator.writeEndObject();
    }
}
