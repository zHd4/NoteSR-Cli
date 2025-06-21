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
public class NotesTableRowDto {
    private String noteId;
    private String noteShortName;
    private String noteShortText;
    private LocalDateTime noteUpdatedAt;
    private Long attachedFilesCount;
}
