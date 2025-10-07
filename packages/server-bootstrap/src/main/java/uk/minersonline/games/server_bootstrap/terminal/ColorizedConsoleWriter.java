package uk.minersonline.games.server_bootstrap.terminal;

import org.tinylog.Level;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColorizedConsoleWriter implements Writer {
    public ColorizedConsoleWriter(Map<String, String> ignoredProperties) {
        // We need this constructor for tinylog
    }

    @Override
    public void write(LogEntry logEntry) {
        String colorCode = getColorForLevel(logEntry.getLevel());
        String resetCode = "\033[0m";

        String formattedMessage = "[{date: HH:mm:ss}] {level}: {message}"
                .replace("{date: HH:mm:ss}", logEntry.getTimestamp().toInstant().toString())
                .replace("{level}", logEntry.getLevel().toString())
                .replace("{thread}", logEntry.getThread().getName())
                .replace("{class-name}", logEntry.getClassName())
                .replace("{method}", logEntry.getMethodName())
                .replace("{message}", logEntry.getMessage());

        System.out.println(colorCode + formattedMessage + resetCode);

        if (logEntry.getException() != null) {
            logEntry.getException().printStackTrace(System.out);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {

    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return List.of(LogEntryValue.LEVEL, LogEntryValue.MESSAGE, LogEntryValue.EXCEPTION);
    }

    private String getColorForLevel(Level level) {
        return switch (level) {
            case ERROR -> "\033[1;31m";  // Bold red
            case WARN  -> "\033[1;33m";  // Bold yellow
            case INFO  -> "\033[0;36m";  // Cyan
            case DEBUG -> "\033[0;34m";  // Blue
            case TRACE -> "\033[2;35m";  // Dim magenta
            default    -> "\033[0;37m";  // White
        };
    }
}
