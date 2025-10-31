package app.notesr.cli.data.dto;

import app.notesr.cli.data.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteOutputDto {
    private Note note;
    private Long attachmentsCount;
}
