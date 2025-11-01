package app.notesr.cli.service.compiler;

import app.notesr.cli.core.security.crypto.AesCryptor;
import app.notesr.cli.core.security.crypto.AesGcmCryptor;
import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupEncryptionException;
import app.notesr.cli.core.exception.BackupIOException;
import app.notesr.cli.data.model.DataBlock;
import app.notesr.cli.data.model.FileInfo;
import app.notesr.cli.data.model.Note;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import static app.notesr.cli.core.util.KeyUtils.getSecretKeyFromSecrets;

@RequiredArgsConstructor
public final class BackupCompiler implements Runnable {

    private final Path dbPath;
    private final Path tempArchivePath;
    private final CryptoSecrets secrets;
    private final String noteSrVersion;

    @Override
    public void run() {
        if (!Files.exists(dbPath)) {
            throw new BackupIOException("Database " + dbPath + " not found");
        }

        File tempArchiveFile = tempArchivePath.toFile();

        DbConnection db = new DbConnection(dbPath.toString());
        AesCryptor cryptor = new AesGcmCryptor(getSecretKeyFromSecrets(secrets));

        try (BackupZipper zipper = new BackupZipper(tempArchiveFile)) {
            writeVersion(zipper);
            writeNotes(zipper, cryptor, db.getConnection().onDemand(NoteEntityDao.class));
            writeFilesInfo(zipper, cryptor, db.getConnection().onDemand(FileInfoEntityDao.class));
            writeFilesData(zipper, cryptor, db.getConnection().onDemand(DataBlockEntityDao.class));
        } catch (IOException e) {
            throw new BackupIOException(e);
        } catch (GeneralSecurityException e) {
            throw new BackupEncryptionException(e);
        }
    }

    private void writeVersion(BackupZipper zipper) throws IOException {
        zipper.addVersionFile(noteSrVersion);
    }

    private void writeNotes(BackupZipper zipper, AesCryptor cryptor, NoteEntityDao noteEntityDao)
            throws IOException, GeneralSecurityException {

        for (Note note : noteEntityDao.getAll()) {
            String json = getObjectMapper().writeValueAsString(note);
            byte[] encryptedJson = cryptor.encrypt(json.getBytes(StandardCharsets.UTF_8));

            zipper.addNote(note.getId(), encryptedJson);
        }
    }

    private void writeFilesInfo(BackupZipper zipper, AesCryptor cryptor, FileInfoEntityDao fileInfoEntityDao)
            throws IOException, GeneralSecurityException {

        for (FileInfo fileInfo : fileInfoEntityDao.getAll()) {
            String json = getObjectMapper().writeValueAsString(fileInfo);
            byte[] encryptedJson = cryptor.encrypt(json.getBytes(StandardCharsets.UTF_8));

            zipper.addFileInfo(fileInfo.getId(), encryptedJson);
        }
    }

    private void writeFilesData(BackupZipper zipper, AesCryptor cryptor, DataBlockEntityDao dataBlockEntityDao)
            throws IOException, GeneralSecurityException {

        for (DataBlock dataBlock : dataBlockEntityDao.getAllDataBlocksWithoutData()) {
            ObjectMapper objectMapper = getObjectMapper();

            ObjectNode blobInfoNode = objectMapper.valueToTree(dataBlock);
            blobInfoNode.remove("data");

            String blobInfoJson = getObjectMapper().writeValueAsString(blobInfoNode);
            byte[] blobData = dataBlockEntityDao.getById(dataBlock.getId()).getData();

            byte[] encryptedBlobInfo = cryptor.encrypt(blobInfoJson.getBytes(StandardCharsets.UTF_8));
            byte[] encryptedBlobData = cryptor.encrypt(blobData);

            zipper.addBlob(dataBlock.getId(), encryptedBlobInfo, encryptedBlobData);
        }
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
