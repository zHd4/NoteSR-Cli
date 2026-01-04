/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BackupDecryptionException extends Exception {
    public BackupDecryptionException(Exception e) {
        super(e);
    }

    public BackupDecryptionException(String message) {
        super(message);
    }
}
