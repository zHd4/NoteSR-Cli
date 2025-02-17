package app.notesr.cli.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Note {
    private String id;
    private String name;
    private String text;
    private LocalDateTime updatedAt;
}
