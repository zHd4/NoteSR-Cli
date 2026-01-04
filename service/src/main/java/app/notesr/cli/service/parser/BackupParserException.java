/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */

package app.notesr.cli.service.parser;

public class BackupParserException extends RuntimeException {
    public BackupParserException(String message) {
        super(message);
    }

    public BackupParserException(String message, Throwable e) {
        super(message, e);
    }
}
