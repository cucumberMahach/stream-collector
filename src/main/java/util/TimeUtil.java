package util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtil {
    public static ZonedDateTime getZonedNow(){
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        return ZonedDateTime.ofInstant(Instant.now(), zoneId);
    }
}
