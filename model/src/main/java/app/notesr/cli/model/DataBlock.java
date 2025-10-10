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
    @JsonAlias("fileId")
    private String fileId;

    @JsonProperty("order")
    private Long blockOrder;

    @JsonProperty("data")
    private byte[] data;
}
