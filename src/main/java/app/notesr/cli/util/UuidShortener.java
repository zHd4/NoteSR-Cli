package app.notesr.cli.util;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
public final class UuidShortener {
    private static final int UUID_BYTES_LENGTH = 16;
    private static final int SHORT_UUID_LENGTH = 22;
    private static final int LONG_UUID_LENGTH = 36;

    private final String uuid;

    public String getShortUuid() {
        if (uuid.length() == SHORT_UUID_LENGTH && uuid.matches("^[A-Za-z0-9_-]{22}$")) {
            return uuid;
        }

        UUID uuidObj = UUID.fromString(uuid);
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[UUID_BYTES_LENGTH]);

        byteBuffer.putLong(uuidObj.getMostSignificantBits());
        byteBuffer.putLong(uuidObj.getLeastSignificantBits());

        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }

    public String getLongUuid() {
        if (uuid.length() == LONG_UUID_LENGTH && uuid.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-"
                + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return uuid;
        }

        byte[] bytes = Base64.getUrlDecoder().decode(uuid);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        long mostSigBits = byteBuffer.getLong();
        long leastSigBits = byteBuffer.getLong();

        return new UUID(mostSigBits, leastSigBits).toString();
    }
}
