package com.telcobright.SmsPoller.repositories;

import com.telcobright.SmsPoller.models.CampaignTask;
import com.telcobright.SmsPoller.models.PollingTask;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PollingTaskRepository extends CrudRepository<PollingTask, String> {
    @Query("select pt from polling_task pt")
    List<CampaignTask> findList(Pageable pageable);

    @Query("SELECT pt FROM polling_task pt WHERE pt.status = ?1")
    List<PollingTask> findIncompletePollingTasks(String status, Pageable pageable);

    @Query("select pt from polling_task pt where pt.status not in ?1")
    List<PollingTask> findIncompletePollingTasksByFinalStatusList(List<String> finalStatusList, Pageable pageable);

//    @Query("select pt from polling_task pt where pt.terminatingCalledNumber in ?1")
//    List<PollingTask> findIncompletePollingTasksByTestNumber(List<String> testNumbers);
}
