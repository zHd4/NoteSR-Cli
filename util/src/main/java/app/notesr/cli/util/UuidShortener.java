package app.notesr.cli.util;

import lombok.RequiredArgsConstructor;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
public final class UuidShortener {
    private static final int UUID_BYTES_LENGTH = 16;
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-"
            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final String SHORT_UUID_REGEX = "^[A-Za-z0-9_-]{22}$";

    private final String uuid;

    public String getShortUuid() {
        if (uuid.matches(SHORT_UUID_REGEX)) {
            return uuid;
        }

        if (!uuid.matches(UUID_REGEX)) {
            throw new IllegalArgumentException("Invalid UUID format: " + uuid);
        }
        UUID uuidObj = UUID.fromString(uuid);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[UUID_BYTES_LENGTH]);

        byteBuffer.putLong(uuidObj.getMostSignificantBits());
        byteBuffer.putLong(uuidObj.getLeastSignificantBits());

        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }

    public String getLongUuid() {
        if (uuid.matches(UUID_REGEX)) {
            return uuid;
        }

        if (!uuid.matches(SHORT_UUID_REGEX)) {
            throw new IllegalArgumentException("Invalid shortened UUID: " + uuid);
        }

        try {
            byte[] bytes = Base64.getUrlDecoder().decode(uuid);

            if (bytes.length != UUID_BYTES_LENGTH) {
                throw new IllegalArgumentException("Base64 UUID must be " + UUID_BYTES_LENGTH + " bytes long");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

            long mostSigBits = byteBuffer.getLong();
            long leastSigBits = byteBuffer.getLong();

            return new UUID(mostSigBits, leastSigBits).toString();
        } catch (BufferUnderflowException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Error decoding UUID: " + uuid, e);
        }
    }
}
