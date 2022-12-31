package com.telcobright.SmsPoller.scheduling;

import com.telcobright.SmsPoller.GpSmsSender;
import com.telcobright.SmsPoller.models.CampaignTask;
import com.telcobright.SmsPoller.repositories.CampaignTaskRepository;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsSchedulerJob implements Job {
    @Autowired
    private CampaignTaskRepository campaignTaskRepository;

    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        CampaignTaskRepository campaignTaskRepository = (CampaignTaskRepository) dataMap.get("campaignTaskRepository");
        CampaignTask campaignTask = (CampaignTask) dataMap.get("campaignTask");
        GpSmsSender.sendSms(campaignTask);

        campaignTaskRepository.save(campaignTask);
    }
}
