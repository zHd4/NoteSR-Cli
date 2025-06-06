package app.notesr.cli.util.mapper;

import app.notesr.cli.model.Note;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class NotesJsonMapper extends JsonMapper<Note> {
    @Override
    public List<Note> map(String json) throws IOException {
        List<Map<String, Object>> maps = parseJson(json);
        return maps.stream()
                .map(line -> Note.builder()
                        .id((String) line.get("id"))
                        .name((String) line.get("name"))
                        .text((String) line.get("text"))
                        .updatedAt(parseDateTime((String) line.get("updated_at")))
                        .build())
                .toList();
    }
}
