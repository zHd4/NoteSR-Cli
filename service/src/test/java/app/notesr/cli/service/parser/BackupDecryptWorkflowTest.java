package app.notesr.cli.service.parser;

import app.notesr.cli.core.exception.BackupDecryptionException;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.service.parser.BackupParserException;
import app.notesr.cli.service.parser.BackupDecryptionService;
import app.notesr.cli.service.parser.BackupDecryptWorkflow;
import app.notesr.cli.service.parser.BackupParsingService;
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

import static app.notesr.cli.core.util.test.FixtureUtils.getFixturePath;
import static app.notesr.cli.core.util.KeyUtils.getKeyBytesFromHex;
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
    private CryptoSecrets secrets;
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

        secrets = getTestSecrets();
    }

    @Test
    void runWithValidInputsAddsTempFilesToList() throws Exception {
        Path parserTempDir = Path.of("temp-dir");
        List<File> tempFiles = new ArrayList<>();

        when(backupDecryptionService.decrypt(encrypted, secrets)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output, secrets)).thenReturn(parserTempDir);

        workflow.run(encrypted, secrets, output, tempFiles);

        assertEquals(2, tempFiles.size(), "Temp files list should contain 2 files");
        assertTrue(tempFiles.contains(decrypted), "Temp files should contain decrypted file");
        assertTrue(tempFiles.contains(parserTempDir.toFile()), "Temp files should contain temp dir file");
    }

    @Test
    void runWithDecryptionFailureThrowsFileDecryptionException() throws Exception {
        List<File> tempFiles = new ArrayList<>();
        when(backupDecryptionService.decrypt(encrypted, secrets)).thenThrow(new BackupDecryptionException());
        assertThrows(BackupDecryptionException.class, () ->
                        workflow.run(encrypted, secrets, output, tempFiles),
                "Decryption failure should throw BackupDecryptionException");
    }

    @Test
    void runWithParsingFailureThrowsBackupParserException() throws Exception {
        List<File> tempFiles = new ArrayList<>();

        when(backupDecryptionService.decrypt(encrypted, secrets)).thenReturn(decrypted);
        when(parsingService.parse(decrypted, output, secrets)).thenThrow(new BackupParserException("Invalid format"));

        assertThrows(BackupParserException.class, () ->
                        workflow.run(encrypted, secrets, output, tempFiles),
                "Parsing failure should throw BackupParserException");
    }

    private CryptoSecrets getTestSecrets() throws IOException {
        String keyHex = Files.readString(getFixturePath("shared/crypto_key.txt", tempDir));
        return new CryptoSecrets(getKeyBytesFromHex(keyHex));
    }
}
