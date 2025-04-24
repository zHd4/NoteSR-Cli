package app.notesr.cli;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class CliSpinner {
    private static final int ANIMATION_DELAY = 150;
    private static final String[] FRAMES = {"|", "/", "-", "\\"};

    private final String text;

    private Logger log = LoggerFactory.getLogger(CliSpinner.class);

    private AtomicInteger frameIndex;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;

    @Setter
    private PrintStream printStream = System.out;

    private boolean running = false;

    public CliSpinner(String text, Logger logger) {
        this.text = text;
        this.log = logger;
    }

    public boolean isAvailable() {
        return !(log.isInfoEnabled() || log.isDebugEnabled() || log.isTraceEnabled());
    }

    public void start() {
        if (running) {
            throw new UnsupportedOperationException("Animation already running");
        }

        if (!isAvailable()) {
            throw new UnsupportedOperationException("Animation cannot be run if logging is enabled");
        }

        running = true;

        Runnable animationTask = () ->
                printStream.print("\r" + text + " " + FRAMES[frameIndex.getAndIncrement() % FRAMES.length]);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        frameIndex = new AtomicInteger(0);
        future = scheduler.scheduleAtFixedRate(animationTask, 0, ANIMATION_DELAY, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (!running) {
            throw new UnsupportedOperationException("Animation already stopped");
        }

        running = false;

        future.cancel(true);
        scheduler.shutdown();

        printStream.print("\r");

        for (int i = 0; i < text.length() + 2; i++) {
            printStream.print("\0");
        }
    }
}
