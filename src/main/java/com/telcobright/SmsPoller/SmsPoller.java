package com.telcobright.SmsPoller;

import com.telcobright.SmsPoller.models.DeliveryStatus;
import com.telcobright.SmsPoller.models.PollingTask;

public interface SmsPoller {
    DeliveryStatus poll(PollingTask task);
}
