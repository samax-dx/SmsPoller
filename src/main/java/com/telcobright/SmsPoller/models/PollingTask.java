package com.telcobright.SmsPoller.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "polling_task")
public class PollingTask {

//    @Id
//    @GenericGenerator(name = "OfbizEntityIdGenerator", strategy = "com.telcobright.SmsPoller.StringSequenceIdentifier")
//    @GeneratedValue(generator = "OfbizEntityIdGenerator")
//    @Column(name="polling_task_id", length = 20)
//    public String pollingTaskId;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="polling_task_id", length = 20)
    public Long pollingTaskId;

    @Column(name="phone_number")
    public String phoneNumber;

    @Column(name="terminating_called_number")
    public String terminatingCalledNumber;

    @Column(name="originating_calling_number")
    public String originatingCallingNumber;
    @Column(name="terminating_calling_number")
    public String terminatingCallingNumber;
    @Column(name="message")
    public String message;
    @Column(name="campaign_id")
    public String campaignId;
    @Column(name="package_id")
    public String packageId;
    @Column(name="route_id")
    public String routeId;
    @Column(name="status")
    public String status;
    @Column(name="error_code")
    public String errorCode;
    @Column(name="task_id")
    public String taskId;
    @Column(name="poll_retry_count")
    public Integer pollRetryCount;
    @Column(name="task_detail_json")
    public String taskDetailJson;
    @Column(name="expires")
    public LocalDateTime expires;
}
