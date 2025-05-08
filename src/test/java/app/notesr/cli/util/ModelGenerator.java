package app.notesr.cli.util;

import app.notesr.cli.model.DataBlock;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static java.util.UUID.randomUUID;

public class ModelGenerator {
    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    public static Note generateTestNote() {
        return Note.builder()
                .id(randomUUID().toString())
                .name(FAKER.text().text(5, 15))
                .text(FAKER.text().text())
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    public static FileInfo generateTestFileInfo(Note note, long size) {
        return FileInfo.builder()
                .id(randomUUID().toString())
                .noteId(note.getId())
                .size(size)
                .name(FAKER.text().text(5, 15))
                .createdAt(truncateDateTime(LocalDateTime.now()))
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    public static DataBlock generateTestDataBlock(FileInfo fileInfo, int size, long order) {
        byte[] data = new byte[size];
        RANDOM.nextBytes(data);

        return DataBlock.builder()
                .id(randomUUID().toString())
                .fileId(fileInfo.getId())
                .order(order)
                .data(data)
                .build();
    }

    public static Set<Note> generateTestNotes(int count) {
        Set<Note> testNotes = new LinkedHashSet<>();

        for (int i = 0; i < count; i++) {
            Note testNote = generateTestNote();
            testNotes.add(testNote);
        }

        return testNotes;
    }

    public static Set<FileInfo> generateTestFilesInfos(Note note, int count, long minFilesSize, long maxFilesSize) {
        Set<FileInfo> testFilesInfos = new LinkedHashSet<>();

        for (int i = 0; i < count; i++) {
            FileInfo testFileInfo = generateTestFileInfo(note, RANDOM.nextLong(minFilesSize, maxFilesSize));
            testFilesInfos.add(testFileInfo);
        }

        return testFilesInfos;
    }

    public static Set<DataBlock> generateTestDataBlocks(FileInfo fileInfo, int blockSize) {
        Set<DataBlock> blocks = new LinkedHashSet<>();

        long order = 0;
        long bytesLeft = fileInfo.getSize();

        while (bytesLeft > blockSize) {
            blocks.add(generateTestDataBlock(fileInfo, blockSize, order));

            order++;
            bytesLeft -= blockSize;
        }

        if (bytesLeft > 0) {
            order++;
            blocks.add(generateTestDataBlock(fileInfo, (int) bytesLeft, order));
        }

        return blocks;
    }
}
