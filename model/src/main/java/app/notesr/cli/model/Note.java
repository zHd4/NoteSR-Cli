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
public class Note {
    private String id;
    private String name;
    private String text;
    private LocalDateTime updatedAt;
}
