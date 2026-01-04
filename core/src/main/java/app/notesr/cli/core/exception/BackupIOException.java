/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.exception;

import java.io.IOException;

public final class BackupIOException extends RuntimeException {
    public BackupIOException(String message) {
        super(message);
    }

    public BackupIOException(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}
