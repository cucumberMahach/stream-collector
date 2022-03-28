package util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;

public class TimeUtil {
    private static final ZoneId zoneId = ZoneId.of("Europe/Moscow");

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
}
