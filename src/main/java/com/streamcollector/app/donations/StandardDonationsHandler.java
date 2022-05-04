package com.streamcollector.app.donations;

import com.streamcollector.app.donations.json.httpDonations.DonationData;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.service.AbstractService;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StandardDonationsHandler extends DonationsHandler{
    private AbstractService service = null;
    private boolean printDebug = false;
    @Override
    protected void onNewDonation(DonationData donation) {
        log(LogStatus.Debug, "NewDonation!");
    }

    @Override
    protected boolean isDonationActual(DonationData donation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoned = ZonedDateTime.parse("2022-05-03 23:35:34", formatter.withZone(TimeUtil.zoneId));
        log(LogStatus.Debug, donation.getCreatedAt(true) + " * " + zoned);
        return donation.getCreatedAt(true).isAfter(zoned);
    }

    public void setService(AbstractService service) {
        this.service = service;
    }

    public void setPrintDebug(boolean printDebug) {
        this.printDebug = printDebug;
    }

    @Override
    public void log(LogStatus status, String message) {
        if (service != null) {
            if (!printDebug && status == LogStatus.Debug)
                return;
            service.writeLog(status, message);
        }
    }
}
