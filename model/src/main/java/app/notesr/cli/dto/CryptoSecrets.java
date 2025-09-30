package app.notesr.cli.dto;

import java.io.Serializable;
import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CryptoSecrets implements Serializable {

    private byte[] key;

    public static CryptoSecrets from(CryptoSecrets cryptoKey) {
        return new CryptoSecrets(Arrays.copyOf(cryptoKey.key, cryptoKey.key.length));
    }
}
