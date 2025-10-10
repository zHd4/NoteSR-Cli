package app.notesr.cli.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileDecryptionException extends Exception {
    public FileDecryptionException(Exception e) {
        super(e);
    }

    public FileDecryptionException(String message) {
        super(message);
    }
}
