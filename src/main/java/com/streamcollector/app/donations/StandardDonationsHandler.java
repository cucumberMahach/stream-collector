package com.streamcollector.app.donations;

import com.streamcollector.app.database.DonationsDatabase;
import com.streamcollector.app.database.entities.TgPaymentEntity;
import com.streamcollector.app.donations.json.httpDonations.DonationData;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.service.DonationsService;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;

public class StandardDonationsHandler extends DonationsHandler {
    private DonationsService service = null;
    private boolean printDebug = false;
    private ZonedDateTime httpLastDonationTime;

    @Override
    protected void onNewDonation(DonationData donation) {
        var session = service.updateSession();

        TgPaymentEntity payment = new TgPaymentEntity();
        payment.donationId = donation.id;
        payment.amount = donation.amount.intValue();
        payment.donationTime = donation.getCreatedAt(true);
        payment.getTime = TimeUtil.getZonedNow();
        payment.message = donation.message.trim();
        payment.title = donation.username;

        var user = DonationsDatabase.getTgUserByDonationKey(session, payment.message);
        if (user != null) {
            payment.tgUser = user;
            user.balance += payment.amount;
            DonationsDatabase.updateTgUser(session, user);
        }
        DonationsDatabase.insertDonation(session, payment);

        log(LogStatus.Success, String.format("New Donation: %d, found user - %b", payment.donationId, user != null));
    }

    @Override
    protected boolean isDonationActual(DonationData donation, boolean http) {
        var session = service.updateSession();

        var donationTime = donation.getCreatedAt(true);
        var donationEntity = DonationsDatabase.getDonationEntity(session);

        if (http) {
            if (donationEntity.lastDonationTime == null) {
                storeLastHttpDonation(donationTime);
                return true;
            } else {
                if (donationTime.isAfter(donationEntity.lastDonationTime)) {
                    storeLastHttpDonation(donationTime);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            donationEntity.lastDonationTime = donationTime;
            DonationsDatabase.updateDonationEntity(session, donationEntity);
            return true;
        }
    }

    private void storeLastHttpDonation(ZonedDateTime donationTime) {
        if (httpLastDonationTime == null || donationTime.isAfter(httpLastDonationTime)) {
            httpLastDonationTime = donationTime;
        }
    }

    @Override
    protected void httpDonationsDone() {
        if (httpLastDonationTime != null) {
            var session = service.updateSession();
            var donationEntity = DonationsDatabase.getDonationEntity(session);
            donationEntity.lastDonationTime = httpLastDonationTime;
            DonationsDatabase.updateDonationEntity(session, donationEntity);
        }
    }

    public void setService(DonationsService service) {
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
