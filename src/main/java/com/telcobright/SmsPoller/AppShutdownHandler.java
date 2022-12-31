package com.telcobright.SmsPoller;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class AppShutdownHandler {
    @PreDestroy
    public void unsetOperational() {
        AppService.operational.set(false);
    }



    @PreDestroy
    public void stopSomeService() {
        System.out.println("some service stopped");
    }
}
