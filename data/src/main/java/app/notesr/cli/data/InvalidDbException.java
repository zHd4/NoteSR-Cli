/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.data;

public class InvalidDbException extends ConnectionException {
    public InvalidDbException(String message, Throwable cause) {
        super(message, cause);
    }
}
