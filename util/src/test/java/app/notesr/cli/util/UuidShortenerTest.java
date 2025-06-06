package app.notesr.cli.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UuidShortenerTest {
    private static final int SHORT_UUID_LENGTH = 22;

    @Test
    void testShortenAndRestore() {
        String originalUuid = UUID.randomUUID().toString();
        UuidShortener shortener = new UuidShortener(originalUuid);

        String shortUuid = shortener.getShortUuid();
        assertNotNull(shortUuid);
        assertEquals(SHORT_UUID_LENGTH, shortUuid.length());

        UuidShortener reverser = new UuidShortener(shortUuid);
        String restoredUuid = reverser.getLongUuid();

        assertEquals(originalUuid, restoredUuid);
    }

    @Test
    void testKnownValue() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        UuidShortener shortener = new UuidShortener(uuid);
        String shortUuid = shortener.getShortUuid();

        UuidShortener reverser = new UuidShortener(shortUuid);
        String restoredUuid = reverser.getLongUuid();

        assertEquals(uuid, restoredUuid);
    }
}
