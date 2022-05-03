package com.streamcollector.app.donations;

import com.streamcollector.app.donations.json.httpDonations.DonationData;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StandardDonationsHandler extends DonationsHandler{
    @Override
    protected void onNewDonation(DonationData donation) {
        System.out.println("NewDonation!");
    }

    @Override
    protected boolean isDonationActual(DonationData donation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoned = ZonedDateTime.parse("2022-05-03 23:35:34", formatter.withZone(TimeUtil.zoneId));
        System.out.print(donation.getCreatedAt(true));
        System.out.print(" * ");
        System.out.println(zoned);
        return donation.getCreatedAt(true).isAfter(zoned);
    }
}
