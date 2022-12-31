package com.telcobright.SmsPoller.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.LocalDateTime;

@Entity(name = "campaign_task")
@IdClass(CampaignTaskKey.class)
public class CampaignTask {
    @Id
    @Column(name = "phone_number")
    public String phoneNumber;

    @Column(name = "terminating_called_number")
    public String terminatingCalledNumber;

    @Column(name = "originating_calling_number")
    public String originatingCallingNumber;

    @Column(name = "terminating_calling_number")
    public String terminatingCallingNumber;

    @Column(name = "message")
    public String message;

    @Id
    @Column(name = "campaign_id")
    public String campaignId;

    @Column(name = "package_id")
    public String packageId;

    @Column(name = "route_id")
    public String routeId;

    @Column(name = "status")
    public String status;

    @Column(name = "status_external")
    public String statusExternal;

    @Column(name = "error_code")
    public String errorCode;

    @Column(name = "error_code_external")
    public String errorCodeExternal;

    @Column(name = "task_id_external")
    public String taskIdExternal;

    @Column(name = "retry_count")
    public Integer retryCount;
    @Column(name = "retry_history")
    public String retryHistory;
    @Column(name = "next_retry_time")
    public String nextRetryTime;

    @Column(name = "last_retry_time")
    public String lastRetryTime;

    @Column(name = "all_retry_times")
    public String allRetryTimes;

    @Column(name = "task_detail_json")
    public String taskDetailJson;

    @Column(name = "created_stamp")
    public LocalDateTime createdStamp;

    @Column(name = "last_updated_stamp")
    public LocalDateTime lastUpdatedStamp;
}
