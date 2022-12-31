package com.telcobright.SmsPoller;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="smspoller")
public class AppProperties {
    public String kafkaServerAddress;
    public String kafkaClientId;
}
