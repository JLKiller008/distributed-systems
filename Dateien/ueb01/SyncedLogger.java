package Dateien.ueb01;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SyncedLogger {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public synchronized void log(String comment) {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        String thread = Thread.currentThread().getName();
        System.out.printf("%s, %s, %s%n", date, thread, comment);
    }
}