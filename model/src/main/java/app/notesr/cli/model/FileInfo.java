package app.notesr.cli.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
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
