package app.notesr.cli.crypto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileDecryptionException extends Exception {
    public FileDecryptionException(Exception e) {
        super(e);
    }
}
