package com.telcobright.SmsPoller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcobright.SmsPoller.models.CampaignTask;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GpSmsSender {
    public static void sendSms(CampaignTask campaignTask) {
        String msisdn = campaignTask.terminatingCalledNumber;
        String message =campaignTask.message;
        campaignTask.retryCount++;
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            restTemplate.exchange(
                    "https://gpcmp.grameenphone.com/ecmapigw/webresources/ecmapigw.v2",
                    HttpMethod.POST,
                    new HttpEntity<>(
                            String.format("{ \"username\": \"ELAdmin_3338\", \"password\": \"Rgl12345^\", \"apicode\": \"6\", \"msisdn\": \"%s\", \"countrycode\": \"880\", \"cli\": \"EXCEELI\", \"messagetype\": \"3\", \"message\": \"%s\", \"messageid\": \"0\" }", msisdn, message),
                            new LinkedMultiValueMap<>(Stream.of(
                                    new AbstractMap.SimpleEntry<>("Content-Type", Collections.singletonList("application/json")),
                                    new AbstractMap.SimpleEntry<>("Accept", Collections.singletonList("application/json"))
                            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    ),
                    String.class
            );

//            ProducerRecord<String, String> producerRecord = createProducerRecordFromCampaignTask(campaignTask);
//            AppService.afterShootProducer.send(producerRecord).get();
        } catch (RestClientException e) {
            campaignTask.errorCode="HTTP Exception";
            //throw new RestClientException("http_client_exception");

        } // catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
    }

    private static ProducerRecord<String, String> createProducerRecordFromCampaignTask(CampaignTask task) {
        Map<String, Object> message = new HashMap<>();
        message.put("tasks", Collections.singletonList(task));
        message.put("routeId", task.routeId);
        message.put("packageId", task.packageId);
        message.put("batchId", task.campaignId + System.currentTimeMillis());

        String key = task.campaignId + System.currentTimeMillis();
        String value = new ObjectMapper().convertValue(message, JsonNode.class).toString();

        return new ProducerRecord<>("smspollv2", key, value);
    }
}
