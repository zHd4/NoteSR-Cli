/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */

package app.notesr.cli.service.parser;

import app.notesr.cli.core.exception.BackupDbException;
import app.notesr.cli.core.exception.BackupIOException;

public interface Parser {
    void parse() throws BackupParserException, BackupDbException, BackupIOException;
}
