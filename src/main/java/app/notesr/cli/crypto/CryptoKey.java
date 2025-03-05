package app.notesr.cli.crypto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.crypto.SecretKey;

@AllArgsConstructor
@Data
@Builder
public class CryptoKey {
    private SecretKey key;
    private byte[] salt;
}
