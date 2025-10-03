package app.notesr.cli.parser.v3;

import app.notesr.cli.crypto.AesCryptor;
import app.notesr.cli.crypto.AesGcmCryptor;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.dto.CryptoSecrets;
import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.parser.BackupParserException;
import app.notesr.cli.parser.Parser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static app.notesr.cli.util.KeyUtils.getSecretKeyFromSecrets;

public class ParserV3 extends Parser {
    private static final String NOTES_DIR = "notes/";
    private static final String FILES_INFO_DIR = "finfo/";
    private static final String FILES_BLOBS_INFO_DIR = "binfo/";
    private static final String FILES_BLOBS_DATA_DIR = "fblobs/";

    private final CryptoSecrets secrets;

    public ParserV3(Path backupPath, Path outputDbPath, CryptoSecrets secrets) {
        super(backupPath, outputDbPath);
        this.secrets = secrets;
    }

    @Override
    public void run() {
        ObjectMapper objectMapper = getObjectMapper();

        AesCryptor cryptor = new AesGcmCryptor(getSecretKeyFromSecrets(secrets));
        DbConnection db = new DbConnection(outputDbPath.toString());

        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);

        try (ZipFile zipFile = new ZipFile(backupPath.toFile())) {
            transferNotes(noteEntityDao, objectMapper, cryptor, zipFile);
            transferFilesInfo(fileInfoEntityDao, objectMapper, cryptor, zipFile);
            transferFilesData(dataBlockEntityDao, objectMapper, cryptor, zipFile);
        } catch (IOException e) {
            throw new BackupParserException("Cannot read file, it's probably corrupted", e);
        }
    }

    private void transferNotes(NoteEntityDao dao, ObjectMapper mapper, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, NOTES_DIR, entry -> {
            try {
                String noteJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                Note note = mapper.readValue(noteJson, Note.class);

                dao.add(note);
            } catch (GeneralSecurityException e) {
                throw new BackupParserException("Cannot decrypt note entry, probably file corrupted", e);
            } catch (IOException e) {
                throw new BackupParserException("Cannot read note entry, probably file corrupted", e);
            }
        });
    }

    private void transferFilesInfo(FileInfoEntityDao dao, ObjectMapper mapper, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, FILES_INFO_DIR, entry -> {
            try {
                String fileInfoJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                FileInfo fileInfo = mapper.readValue(fileInfoJson, FileInfo.class);

                dao.add(fileInfo);
            } catch (GeneralSecurityException e) {
                throw new BackupParserException("Cannot decrypt file info entry, probably file corrupted", e);
            } catch (IOException e) {
                throw new BackupParserException("Cannot read file info entry, probably file corrupted", e);
            }
        });
    }

    private void transferFilesData(DataBlockEntityDao dao, ObjectMapper mapper, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, FILES_BLOBS_INFO_DIR, entry -> {
            try {
                String blobInfoJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                DataBlock dataBlock = mapper.readValue(blobInfoJson, DataBlock.class);

                ZipEntry blobDataEntry = zipFile.getEntry(FILES_BLOBS_DATA_DIR + dataBlock.getId());

                if (blobDataEntry == null) {
                    throw new BackupParserException("Blob data not found for " + dataBlock.getId()
                            + ", probably file corrupted");
                }

                byte[] blobData = decryptBytes(cryptor, readAllBytes(zipFile, blobDataEntry));

                dataBlock.setData(blobData);
                dao.add(dataBlock);
            } catch (GeneralSecurityException e) {
                throw new BackupParserException("Cannot decrypt data block entry, probably file corrupted", e);
            } catch (IOException e) {
                throw new BackupParserException("Cannot read data block entry, probably file corrupted", e);
            }
        });
    }

    private void walk(ZipFile zipFile, String dirPrefix, Consumer<ZipEntry> forEachFileAction) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(dirPrefix)) {
                forEachFileAction.accept(entry);
            }
        }
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private byte[] readAllBytes(ZipFile zipFile, ZipEntry entry) throws IOException {
        try (InputStream is = zipFile.getInputStream(entry);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] tmp = new byte[8192];
            int read;

            while ((read = is.read(tmp)) != -1) {
                buffer.write(tmp, 0, read);
            }

            return buffer.toByteArray();
        }
    }

    private String decryptJson(AesCryptor cryptor, byte[] encryptedJsonBytes) throws GeneralSecurityException {
        return new String(cryptor.decrypt(encryptedJsonBytes), StandardCharsets.UTF_8);
    }

    private byte[] decryptBytes(AesCryptor cryptor, byte[] encryptedBytes) throws GeneralSecurityException {
        return cryptor.decrypt(encryptedBytes);
    }
}
