package app.notesr.cli.data.mapper;

import app.notesr.cli.data.model.Note;
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
                        .createdAt(
                                line.get("created_at") != null
                                        ? parseDateTime((String) line.get("created_at"))
                                        : null
                        )
                        .updatedAt(parseDateTime((String) line.get("updated_at")))
                        .build())
                .toList();
    }
}
