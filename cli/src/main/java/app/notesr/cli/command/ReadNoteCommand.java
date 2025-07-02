package app.notesr.cli.command;

import app.notesr.cli.db.ConnectionException;
import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.NoteOutputDto;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.Note;
import app.notesr.cli.service.NoteReadingService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.MappingException;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;

@Slf4j
@CommandLine.Command(name = "read-note",
        description = "Reads a note stored in a NoteSR Backup Database.")
public final class ReadNoteCommand extends Command {
    private static final int MAX_TEXT_WIDTH = 50;
    private static final int TEXT_PADDING = 1;

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @Setter(AccessLevel.PACKAGE)
    private PrintStream out = System.out;

    public ReadNoteCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        try {
            File dbFile = getFile(dbPath);
            NoteOutputDto noteOutputDto = getNoteOutputDto(dbFile);

            renderNote(noteOutputDto);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } catch (ConnectionException e) {
            log.error(e.getMessage());
            exitCode = DB_ERROR;
        }

        return exitCode;
    }

    private void renderNote(NoteOutputDto noteOutputDto) {
        Note note = noteOutputDto.getNote();
        Long attachmentsCount = noteOutputDto.getAttachmentsCount();

        List<String> wrappedLines = wrapText(note.getText());

        int contentWidth = getMaxLineLength(wrappedLines);
        int boxWidth = contentWidth + 2 * TEXT_PADDING;

        String horizontalBorder = "+" + "-".repeat(boxWidth + 2) + "+";

        out.println();
        out.println(horizontalBorder);
        out.println("| " + centerText(note.getName(), boxWidth) + " |");
        out.println(horizontalBorder);

        for (String line : wrappedLines) {
            out.println("| "
                    + " ".repeat(TEXT_PADDING)
                    + padRight(line, contentWidth)
                    + " ".repeat(TEXT_PADDING) + " |");
        }

        out.println(horizontalBorder);
        out.printf("%nID: %s%n", note.getId());
        out.printf("Updated at: %s%n", dateTimeToString(note.getUpdatedAt()));
        out.printf("Files attached: %s%n", attachmentsCount);
    }

    private NoteOutputDto getNoteOutputDto(File dbFile) throws CommandHandlingException {
        DbConnection db = new DbConnection(dbFile.getAbsolutePath());
        NoteReadingService noteReadingService = new NoteReadingService(db);

        try {
            return noteReadingService.readNote(noteId);
        } catch (NoteNotFoundException e) {
            log.error("{}: note with id '{}' not found", dbPath, noteId);
            throw new CommandHandlingException(DB_ERROR);
        } catch (MappingException | UnableToProduceResultException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    static List<String> wrapText(String text) {
        List<String> lines = new ArrayList<>();

        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (line.length() + word.length() + 1 > MAX_TEXT_WIDTH) {
                    lines.add(line.toString().trim());
                    line = new StringBuilder();
                }

                line.append(word).append(" ");
            }

            if (!line.isEmpty()) {
                lines.add(line.toString().trim());
            }
        }

        return lines;
    }

    private static int getMaxLineLength(List<String> lines) {
        return lines.stream().mapToInt(String::length).max().orElse(0);
    }

    private static String centerText(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, Math.max(0, width - 1)) + "…";
        }

        int padding = width - text.length();
        int padLeft = padding / 2;
        int padRight = padding - padLeft;

        return " ".repeat(padLeft) + text + " ".repeat(padRight);
    }

    private static String padRight(String text, int width) {
        return text + " ".repeat(width - text.length());
    }
}
