package app.notesr.cli.compiler;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.DataBlockEntityDao;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.util.ZipUtils;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.mapper.MappingException;
import org.jdbi.v3.core.result.UnableToProduceResultException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Getter
public final class BackupCompiler implements Runnable {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String VERSION_FILE_NAME = "version";
    private static final String NOTES_JSON_FILE_NAME = "notes.json";
    private static final String FILES_INFO_JSON_FILE_NAME = "files_info.json";
    private static final String DATA_BLOCKS_DIR_NAME = "data_blocks";

    private final Path dbPath;
    private final Path outputPath;
    private final String noteSrVersion;

    @Setter
    private Path tempDirPath;

    public BackupCompiler(Path dbPath, Path outputPath, String noteSrVersion) {
        this.dbPath = dbPath;
        this.outputPath = outputPath;
        this.noteSrVersion = noteSrVersion;
        this.tempDirPath = Path.of(dbPath.toString() + "_temp");
    }

    @Override
    public void run() {
        if (!Files.exists(dbPath)) {
            throw new BackupIOException("Database " + dbPath + " not found");
        }

        try {
            Files.createDirectory(tempDirPath);
            File tempDir = tempDirPath.toFile();

            writeVersion(tempDir);
            writeData(tempDir);

            ZipUtils.zipDirectory(tempDirPath.toString(), outputPath.toString());
        } catch (IOException e) {
            throw new BackupIOException(e);
        } catch (MappingException | UnableToProduceResultException e) {
            throw new BackupDbException(e);
        }
    }

    private void writeVersion(File dir) throws IOException {
        Path versionFilePath = Path.of(dir.getAbsolutePath(), VERSION_FILE_NAME);
        Files.writeString(versionFilePath, noteSrVersion);
    }

    private void writeData(File dir) throws IOException {
        DbConnection db = new DbConnection(dbPath.toString());

        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);
        DataBlockEntityDao dataBlockEntityDao = db.getConnection().onDemand(DataBlockEntityDao.class);

        JsonGenerator noteJsonGenerator = getJsonGenerator(dir, NOTES_JSON_FILE_NAME);
        JsonGenerator fileInfoJsonGenerator = getJsonGenerator(dir, FILES_INFO_JSON_FILE_NAME);

        NoteWriter noteWriter = new NoteWriter(noteJsonGenerator, noteEntityDao, noteSrVersion, DATETIME_FORMATTER);
        noteWriter.write();

        FileInfoWriter fileInfoWriter = new FileInfoWriter(fileInfoJsonGenerator, fileInfoEntityDao,
                dataBlockEntityDao, DATETIME_FORMATTER);
        fileInfoWriter.write();

        File dataBlocksDir = new File(dir, DATA_BLOCKS_DIR_NAME);
        FileDataWriter fileDataWriter = new FileDataWriter(dataBlocksDir, dataBlockEntityDao);
        fileDataWriter.write();
    }

    private JsonGenerator getJsonGenerator(File dir, String filename) throws IOException {
        File file = new File(dir, filename);
        JsonFactory jsonFactory = new JsonFactory();
        return jsonFactory.createGenerator(file, JsonEncoding.UTF8);
    }
}
