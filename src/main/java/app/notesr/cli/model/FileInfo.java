package app.notesr.cli.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileInfo {
    private String id;
    private String noteId;
    private Long size;
    private String name;
    private String type;
    private byte[] thumbnail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
