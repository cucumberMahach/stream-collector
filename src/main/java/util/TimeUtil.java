package util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;

public class TimeUtil {
    private static final ZoneId zoneId = ZoneId.of("Europe/Moscow");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ZonedDateTime getZonedNow(){
        return ZonedDateTime.ofInstant(Instant.now(), zoneId);
    }

    public static ZonedDateTime getZonedFromUnix(long unix){
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), zoneId);
    }

    public static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }

    public static String formatDurationDays(Duration duration){
        return String.format("%d д %02d ч %02d мин %02d сек",
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    public static String formatZoned(ZonedDateTime zoned){
        return formatter.format(zoned);
    }

    public static ZonedDateTime fromLocalDate(LocalDate localDate){
        return localDate.atStartOfDay().atZone(zoneId);
    }

    public static ZonedDateTime maxZoned(){
        return ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 0, zoneId);
    }
}
