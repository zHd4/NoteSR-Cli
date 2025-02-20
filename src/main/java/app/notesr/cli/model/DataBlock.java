package app.notesr.cli.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataBlock {
    private String id;
    private String fileId;
    private Long order;
    private byte[] data;
}
