package app.notesr.cli.dto;

import app.notesr.cli.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteOutputDto {
    private Note note;
    private Long attachmentsCount;
}
