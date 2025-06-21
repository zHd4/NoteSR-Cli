package app.notesr.cli.util.mapper;

import app.notesr.cli.model.DataBlock;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public final class DataBlocksJsonMapper extends JsonMapper<DataBlock> {
    @Override
    public List<DataBlock> map(String json) throws IOException {
        List<Map<String, Object>> maps = parseJson(json);
        return maps.stream()
                .map(line -> DataBlock.builder()
                        .id((String) line.get("id"))
                        .fileId((String) line.get("file_id"))
                        .blockOrder(Long.valueOf((Integer) line.get("block_order")))
                        .data(parseDataBlockData(line.get("data")))
                        .build())
                .toList();
    }

    private static byte[] parseDataBlockData(Object data) {
        if (data instanceof String) {
            return Base64.getDecoder().decode(String.valueOf(data));
        } else if (data instanceof byte[] bytes) {
            return bytes;
        } else {
            throw new IllegalArgumentException("Unexpected instance");
        }
    }
}
