package com.telcobright.SmsPoller;

import com.telcobright.SmsPoller.models.DeliveryStatus;
import com.telcobright.SmsPoller.models.PollingTask;

public class Ss7SmsPoller implements SmsPoller {
    @Override
    public DeliveryStatus poll(PollingTask task) {
        return DeliveryStatus.valueOf(task.status);
    }
}
