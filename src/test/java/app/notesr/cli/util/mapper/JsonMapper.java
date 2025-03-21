package app.notesr.cli.util.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public abstract class JsonMapper<T> {
    protected static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    protected final List<Map<String, Object>> parseJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, new TypeReference<>() { });
    }

    public abstract List<T> map(String json) throws IOException;
}
