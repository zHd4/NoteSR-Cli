package app.notesr.cli.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FilesTableRowDto {
    private String id;
    private String fileName;
    private Long fileSize;
    private LocalDateTime updatedAt;
}
