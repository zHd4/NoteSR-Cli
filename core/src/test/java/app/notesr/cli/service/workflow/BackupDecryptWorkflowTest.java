package app.notesr.cli.service.workflow;

import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.parser.BackupParserException;
import app.notesr.cli.service.BackupDecryptionService;
import app.notesr.cli.service.BackupParsingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupDecryptWorkflowTest {
    @Mock
    private BackupDecryptionService backupDecryptionService;

    @Mock
    private BackupParsingService parsingService;

    private BackupDecryptWorkflow workflow;
    private File encrypted;
    private CryptoKey cryptoKey;
    private File output;
    private File decrypted;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        workflow = new BackupDecryptWorkflow(backupDecryptionService, parsingService);

        encrypted = new File("file.notesr.bak");
        output = new File("output.db");
        decrypted = new File("file.notesr.bak_decrypted");

        cryptoKey = getTestKey();
    }

    @Test
    void runWithValidInputsAddsTempFilesToList() throws Exception {
        Path parserTempDir = Path.of("temp-dir");
        List<File> tempFiles = new ArrayList<>();

        when(backupDecryptionService.decrypt(encrypted, cryptoKey)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output)).thenReturn(parserTempDir);

        workflow.run(encrypted, cryptoKey, output, tempFiles);

        assertEquals(2, tempFiles.size(), "Temp files list should contain 2 files");
        assertTrue(tempFiles.contains(decrypted), "Temp files should contain decrypted file");
        assertTrue(tempFiles.contains(parserTempDir.toFile()), "Temp files should contain temp dir file");
    }

    @Test
    void runWithDecryptionFailureThrowsFileDecryptionException() throws Exception {
        List<File> tempFiles = new ArrayList<>();
        when(backupDecryptionService.decrypt(encrypted, cryptoKey)).thenThrow(new FileDecryptionException());
        assertThrows(FileDecryptionException.class, () ->
                        workflow.run(encrypted, cryptoKey, output, tempFiles),
                "Decryption failure should throw FileDecryptionException");
    }

    @Test
    void runWithParsingFailureThrowsBackupParserException() throws Exception {
        List<File> tempFiles = new ArrayList<>();

        when(backupDecryptionService.decrypt(encrypted, cryptoKey)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output)).thenThrow(new BackupParserException("Invalid format"));

        assertThrows(BackupParserException.class, () ->
                        workflow.run(encrypted, cryptoKey, output, tempFiles),
                "Parsing failure should throw BackupParserException");
    }

    private CryptoKey getTestKey() throws IOException {
        return hexToCryptoKey(Files.readString(getFixturePath("crypto_key.txt", tempDir)),
                KEY_GENERATOR_ALGORITHM);
    }
}
