package app.notesr.cli.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"order"})
    private Long blockOrder;

    private byte[] data;
}
