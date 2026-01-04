/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */

package app.notesr.cli.service.parser;

public class UnexpectedFieldException extends RuntimeException {
    public UnexpectedFieldException(String message) {
        super(message);
    }
}
