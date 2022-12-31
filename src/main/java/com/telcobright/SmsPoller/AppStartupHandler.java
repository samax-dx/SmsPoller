package com.telcobright.SmsPoller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcobright.SmsPoller.models.CampaignTask;
import com.telcobright.SmsPoller.models.DeliveryStatus;
import com.telcobright.SmsPoller.models.FinalDeliveryStatus;
import com.telcobright.SmsPoller.models.PollingTask;
import com.telcobright.SmsPoller.repositories.CampaignTaskRepository;
import com.telcobright.SmsPoller.repositories.PollingTaskRepository;
import com.telcobright.SmsPoller.scheduling.SmsScheduler;
import com.telcobright.SmsPoller.util.JsonBase64Helper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;


@Component
public class AppStartupHandler {
    ConcurrentHashMap<String, List<CampaignTask>> messageIdWisePollingTasks= new ConcurrentHashMap<>();

    @Autowired
    CampaignTaskRepository campaignTaskRepository;

    @Autowired
    PollingTaskRepository pollingTaskRepository;

    @PostConstruct
    public void instantiateScheduler() {
        try {
            AppService.scheduler = new StdSchedulerFactory().getScheduler();
            AppService.scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void instantiateAfterShootProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", Optional.ofNullable(AppService.config.kafkaServerAddress).orElse("localhost:9092"));
        properties.put("client.id", Optional.ofNullable(AppService.config.kafkaClientId).orElse("pollingClient"));

        AppService.afterShootProducer = new KafkaProducer<>(properties, new StringSerializer(), new StringSerializer());
    }


    @PostConstruct
    public void setupKafkaConsumer() {
        Executors.newSingleThreadExecutor().submit(() -> {
            Properties properties = new Properties();
            properties.put("bootstrap.servers", Optional.ofNullable(AppService.config.kafkaServerAddress).orElse("localhost:9092"));
            properties.put("group.id", Optional.ofNullable(AppService.config.kafkaClientId).orElse("pollingClient"));

            KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer());

            while (!AppService.operational.get()) {
                LockSupport.parkNanos(1000 * 10);
            }

            kafkaConsumer.subscribe(Collections.singleton("smspollv2"));

            while (AppService.operational.get()) {
                for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(100))) {
                    System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s%n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                    try {
                        ConsumerRecord<String, String> consumedDataForPolling =
                                new ConsumerRecord<>("smspollv2", 0, 0, record.key(), record.value());
                        List<PollingTask> pollingTasks = getPollingTasksFromKafkaMsg(consumedDataForPolling.value());
                        pollingTaskRepository.saveAll(pollingTasks);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }

            kafkaConsumer.close();
        });
    }

    List<PollingTask> getPollingTasksFromKafkaMsg(String json) {
        JsonNode pollingMessage;
        try {
            pollingMessage = new ObjectMapper().readValue(json, JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
         
        JsonNode campaignTasks=pollingMessage.get("tasks");
        List<PollingTask> pollingTasks= new ArrayList<>();
        
        campaignTasks.forEach(campaignTask->{
            PollingTask task= new PollingTask();
            task.phoneNumber=campaignTask.get("phoneNumber").asText();
            task.terminatingCalledNumber=campaignTask.get("terminatingCalledNumber").asText();
            task.originatingCallingNumber=campaignTask.get("originatingCallingNumber").asText();
            task.terminatingCallingNumber=campaignTask.get("terminatingCallingNumber").asText();
            task.message=campaignTask.get("message").asText();
            task.campaignId=campaignTask.get("campaignId").asText();
            task.packageId=campaignTask.get("packageId").asText();
            task.routeId=campaignTask.get("routeId").asText();
            task.status=campaignTask.get("statusExternal").asText();
            task.errorCode=campaignTask.get("errorCode").asText();
            task.taskId=campaignTask.get("taskIdExternal").asText();
            task.pollRetryCount=0;
            task.taskDetailJson=campaignTask.has("taskDetailJson") ? campaignTask.get("taskDetailJson").asText() : null;
            task.expires=LocalDateTime.now().plusDays(3);
            pollingTasks.add(task);
        });
        return pollingTasks;
    }

    @PostConstruct
    public void runPollingCampaignLoop() {

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
            List<PollingTask> pollingTasks= pollingTaskRepository
                    .findIncompletePollingTasksByFinalStatusList(FinalDeliveryStatus.getFinalDeliveryStatusList(),
                            PageRequest.of(0,1000));
            //List<String> testNumbers= Arrays.asList("01754105098","01711533920");
            List<String> testNumbers= Arrays.asList("017134567899");
            List<PollingTask> pollingTasksByTestNumbers= pollingTaskRepository.findIncompletePollingTasksByTestNumber(testNumbers);
            pollingTasks.addAll(pollingTasksByTestNumbers);

            pollingTasks.forEach(pollingTask->{
                try{
                    DeliveryStatus deliveryStatus = new GpSmsPoller().poll(pollingTask);
                    CampaignTask campaignTask = campaignTaskRepository.findByPhoneNumberAndCampaignId(pollingTask.phoneNumber, pollingTask.campaignId);

                    if (campaignTask == null) {
                        return;
                    }

                    pollingTask.status=deliveryStatus.name();
                    campaignTask.statusExternal=deliveryStatus.name();

                    int lastRetryCount=campaignTask.retryCount==null?0: campaignTask.retryCount;
                    int newRetryCount=lastRetryCount+1;
                    String retryHistoryStr=campaignTask.retryHistory==null?"":campaignTask.retryHistory;
                    Map<String,String> newHistory = new HashMap<>();
                    newHistory.put("retryCount:",newRetryCount+"");

                    LocalDateTime currentTime = LocalDateTime.now();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = currentTime.format(dateTimeFormatter);
                    newHistory.put("time:", formattedDate);
                    newHistory.put("statusExternal:", deliveryStatus.name());
                    retryHistoryStr= retryHistoryStr.equals("") ?JsonBase64Helper.encodeField(newHistory):
                            retryHistoryStr + "," + JsonBase64Helper.encodeField(newHistory);
                    campaignTask.retryHistory=retryHistoryStr;
                pollingTaskRepository.save(pollingTask);
                    campaignTaskRepository.save(campaignTask);

                    if(testNumbers.contains(campaignTask.terminatingCalledNumber)||
                            (FinalDeliveryStatus.getFailedDeliveryStatusList().contains(deliveryStatus.name()))){//not final status
                        scheduleSmsForRetry(campaignTask);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        },0, 5000, TimeUnit.MILLISECONDS);

    }

    void scheduleSmsForRetry(CampaignTask campaignTask) {
        List<LocalDateTime> nextSchedules = Arrays.stream(campaignTask.allRetryTimes.split(","))
                .map(t -> Instant.ofEpochMilli(Long.parseLong(t)).atOffset(ZoneOffset.UTC).toLocalDateTime()).collect(Collectors.toList());

        Integer retryCount = campaignTask.retryCount;
        if (!(nextSchedules.size() > retryCount)) {
            return;
        }
        try {
            campaignTask.lastRetryTime = nextSchedules.get(retryCount).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + "";
            campaignTask.nextRetryTime = nextSchedules.get(retryCount + 1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + "";
        }
        catch(Exception e){}
        LocalDateTime nextRun = nextSchedules.get(retryCount);
        Date nextRunAsDate=Date.from(nextRun.atZone(ZoneId.systemDefault()).toInstant());

        try {
            SmsScheduler.scheduleFutureSms(campaignTaskRepository, campaignTask, nextRunAsDate);
        } catch (SchedulerException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    @PostConstruct
    public void setOperational() {
        AppService.operational.set(true);
    }
}
