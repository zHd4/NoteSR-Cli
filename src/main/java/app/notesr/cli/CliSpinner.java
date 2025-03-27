package app.notesr.cli;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class CliSpinner {
    private static final int ANIMATION_DELAY = 150;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final String text;

    private Thread loaderThread;

    @Setter
    private PrintStream printStream = System.out;

    public void start() {
        loaderThread = new Thread(() -> {
            String[] frames = {"|", "/", "-", "\\"};
            int i = 0;

            while (running.get()) {
                printStream.print("\r" + text + " " + frames[i++ % frames.length]);

                try {
                    Thread.sleep(ANIMATION_DELAY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            printStream.print("\r");
        });

        running.set(true);
        loaderThread.start();
    }

    public void stop() throws InterruptedException {
        running.set(false);
        loaderThread.join();
    }
}
