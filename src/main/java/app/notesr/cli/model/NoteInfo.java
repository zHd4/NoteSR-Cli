package app.notesr.cli.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NoteInfo {
    private long id;
    private String title;
}
