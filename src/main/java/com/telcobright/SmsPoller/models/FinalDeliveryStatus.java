package com.telcobright.SmsPoller.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FinalDeliveryStatus {
    public static List<String> getFinalDeliveryStatusList(){
        return Stream.of(
                        DeliveryStatus.delivered,
                        DeliveryStatus.rejected)
                .map(Enum::name).collect(Collectors.toList());
    }
    public static List<String> getFailedDeliveryStatusList(){
        return Stream.of(
                        DeliveryStatus.failed
                        )
                .map(Enum::name).collect(Collectors.toList());
    }
}
