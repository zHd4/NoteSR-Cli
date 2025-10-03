package app.notesr.cli.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DataBlock {

    @JsonProperty("id")
    private String id;

    @JsonProperty("file_id")
    private String fileId;

    @JsonAlias({"order"})
    private Long blockOrder;

    @JsonProperty("data")
    private byte[] data;
}
