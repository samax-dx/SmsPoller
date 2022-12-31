package com.telcobright.SmsPoller;

import org.apache.kafka.clients.producer.Producer;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AppService {
    public static AppProperties config;

    public static AtomicBoolean operational;
    public  static Producer<String,String> afterShootProducer; // currently: smspoll producer
    public  static Scheduler scheduler;

    @Autowired
    AppService(AppProperties appProperties) {
        AppService.config = appProperties;
        AppService.operational = new AtomicBoolean();
    }
}
