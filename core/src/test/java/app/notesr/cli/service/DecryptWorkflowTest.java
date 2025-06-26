package app.notesr.cli.service;

import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.parser.BackupParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.CryptoKeyUtils.hexToCryptoKey;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class DecryptWorkflowTest {
    private DecryptionService decryptionService;
    private BackupParsingService parsingService;
    private DecryptWorkflow workflow;
    private File encrypted;
    private CryptoKey cryptoKey;
    private File output;
    private File decrypted;

    @BeforeEach
    void setUp() throws Exception {
        decryptionService = mock(DecryptionService.class);
        parsingService = mock(BackupParsingService.class);

        workflow = new DecryptWorkflow(decryptionService, parsingService);

        encrypted = new File("file.notesr.bak");
        output = new File("output.db");
        decrypted = new File("file.notesr.bak_decrypted");

        cryptoKey = getTestKey();
    }

    @Test
    void runWithValidInputsAddsTempFilesToList() throws Exception {
        Path tempDir = Path.of("temp-dir");
        List<File> tempFiles = new ArrayList<>();

        when(decryptionService.decrypt(encrypted, cryptoKey)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output)).thenReturn(tempDir);

        workflow.run(encrypted, cryptoKey, output, tempFiles);

        assertEquals(2, tempFiles.size(), "Temp files list should contain 2 files");
        assertTrue(tempFiles.contains(decrypted), "Temp files should contain decrypted file");
        assertTrue(tempFiles.contains(tempDir.toFile()), "Temp files should contain temp dir file");
    }

    @Test
    void runWithDecryptionFailureThrowsFileDecryptionException() throws Exception {
        List<File> tempFiles = new ArrayList<>();
        when(decryptionService.decrypt(encrypted, cryptoKey)).thenThrow(new FileDecryptionException());
        assertThrows(FileDecryptionException.class, () ->
                        workflow.run(encrypted, cryptoKey, output, tempFiles),
                "Decryption failure should throw FileDecryptionException");
    }

    @Test
    void runWithParsingFailureThrowsBackupParserException() throws Exception {
        List<File> tempFiles = new ArrayList<>();

        when(decryptionService.decrypt(encrypted, cryptoKey)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output)).thenThrow(new BackupParserException("Invalid format"));

        assertThrows(BackupParserException.class, () ->
                        workflow.run(encrypted, cryptoKey, output, tempFiles),
                "Parsing failure should throw BackupParserException");
    }

    private CryptoKey getTestKey() throws IOException {
        return hexToCryptoKey(Files.readString(getFixturePath("crypto_key.txt")), KEY_GENERATOR_ALGORITHM);
    }
}
