package app.notesr.cli.service.parser.v3;

import app.notesr.cli.core.security.crypto.AesCryptor;
import app.notesr.cli.core.security.crypto.AesGcmCryptor;
import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.data.model.DataBlock;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import app.notesr.cli.service.parser.BackupParserException;
import app.notesr.cli.service.parser.Parser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

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

import static app.notesr.cli.core.util.KeyUtils.getSecretKeyFromSecrets;

@RequiredArgsConstructor
public final class ParserV3 implements Parser {

    private static final String NOTES_DIR = "notes/";
    private static final String FILES_INFO_DIR = "finfo/";
    private static final String FILES_BLOBS_INFO_DIR = "binfo/";
    private static final String FILES_BLOBS_DATA_DIR = "fblobs/";

    private final Path backupPath;
    private final Path outputDbPath;
    private final CryptoSecrets secrets;

    @Override
    public void parse() {
        AesCryptor cryptor = new AesGcmCryptor(getSecretKeyFromSecrets(secrets));
        DbConnection db = new DbConnection(outputDbPath.toString());

        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);

        try (ZipFile zipFile = new ZipFile(backupPath.toFile())) {
            transferNotes(noteEntityDao, cryptor, zipFile);
            transferFilesInfo(fileInfoEntityDao, cryptor, zipFile);
            transferFilesData(dataBlockEntityDao, cryptor, zipFile);
        } catch (IOException e) {
            throw new BackupParserException("Cannot read file, it's probably corrupted", e);
        }
    }

    private void transferNotes(NoteEntityDao dao, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, NOTES_DIR, entry -> {
            try {
                ObjectMapper objectMapper = getObjectMapper();

                String noteJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                Note note = objectMapper.readValue(noteJson, Note.class);

                dao.add(note);
            } catch (GeneralSecurityException e) {
                throw new BackupParserException("Cannot decrypt note entry, probably file corrupted", e);
            } catch (IOException e) {
                throw new BackupParserException("Cannot read note entry, probably file corrupted", e);
            }
        });
    }

    private void transferFilesInfo(FileInfoEntityDao dao, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, FILES_INFO_DIR, entry -> {
            try {
                ObjectMapper objectMapper = getObjectMapper();

                String fileInfoJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                ObjectNode fileInfoNode = (ObjectNode) objectMapper.readTree(fileInfoJson);
                fileInfoNode.remove("decimalId");

                FileInfo fileInfo = objectMapper.treeToValue(fileInfoNode, FileInfo.class);

                dao.add(fileInfo);
            } catch (GeneralSecurityException e) {
                throw new BackupParserException("Cannot decrypt file info entry, probably file corrupted", e);
            } catch (IOException e) {
                throw new BackupParserException("Cannot read file info entry, probably file corrupted", e);
            }
        });
    }

    private void transferFilesData(DataBlockEntityDao dao, AesCryptor cryptor, ZipFile zipFile) {
        walk(zipFile, FILES_BLOBS_INFO_DIR, entry -> {
            try {
                ObjectMapper objectMapper = getObjectMapper();

                String blobInfoJson = decryptJson(cryptor, readAllBytes(zipFile, entry));
                DataBlock dataBlock = objectMapper.readValue(blobInfoJson, DataBlock.class);

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
