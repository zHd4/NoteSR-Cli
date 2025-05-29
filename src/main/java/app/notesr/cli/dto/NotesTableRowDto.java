package app.notesr.cli.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotesTableRowDto {
    private String noteId;
    private String noteShortName;
    private String noteShortText;
    private LocalDateTime noteUpdatedAt;
    private Long attachedFilesCount;
}
