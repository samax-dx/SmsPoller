package com.telcobright.SmsPoller.repositories;

import com.telcobright.SmsPoller.models.CampaignTask;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface CampaignTaskRepository extends CrudRepository<CampaignTask, String> {
    @Query("select ct from campaign_task ct")
    List<CampaignTask> findList(Pageable pageable);

//    @Query("SELECT ct FROM campaign_task ct WHERE ct.phoneNumber=?1 and ct.campaignId=?2")
//    CampaignTask findByPhoneNumberAndCampaignId(String phoneNumber, String campaignId);

    @Query("SELECT ct FROM campaign_task ct WHERE ct.taskIdExternal=?1")
    CampaignTask findByTaskIdExternal(String taskIdExternal);

    @Query("SELECT ct FROM campaign_task ct WHERE ct.campaignTaskId=?1")
    CampaignTask findByCampaignTaskId(String campaignTaskId);
}
