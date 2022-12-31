package com.telcobright.SmsPoller.scheduling;

import com.telcobright.SmsPoller.AppService;
import com.telcobright.SmsPoller.models.CampaignTask;
import com.telcobright.SmsPoller.repositories.CampaignTaskRepository;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;

public class SmsScheduler {

    public static void scheduleFutureSms(CampaignTaskRepository campaignTaskRepository, CampaignTask campaignTask, Date nextRun) throws SchedulerException {
        String jobId = campaignTask.campaignId + "/" + campaignTask.phoneNumber + "/" + nextRun.toString();
        String jobGroup = "smsRetry";

        if (AppService.scheduler.checkExists(JobKey.jobKey(jobId, jobGroup))) {
            return;
        }

        Map<String,Object> data = new HashMap<>();
        data.put("campaignTaskRepository", campaignTaskRepository);
        data.put("campaignTask", campaignTask);
        JobDataMap jobDataMap= new JobDataMap(data);

        JobDetail job = newJob(SmsSchedulerJob.class)
                .withIdentity(jobId, jobGroup)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId, jobGroup)
                .startAt(nextRun)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withMisfireHandlingInstructionFireNow())
                .build();

        AppService.scheduler.scheduleJob(job, trigger);
    }

}
