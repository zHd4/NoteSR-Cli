package app.notesr.cli.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DataBlock {
    private String id;
    private String fileId;
    private Long order;
    private byte[] data;
}
