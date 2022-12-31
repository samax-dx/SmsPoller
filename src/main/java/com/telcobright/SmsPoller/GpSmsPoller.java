package com.telcobright.SmsPoller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcobright.SmsPoller.models.DeliveryStatus;
import com.telcobright.SmsPoller.models.PollingTask;
import com.telcobright.SmsPoller.repositories.CampaignTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GpSmsPoller {
    @Autowired
    CampaignTaskRepository campaignTaskRepository;

    DeliveryStatus poll(PollingTask task) {
        Map<String, Object> response = pollTask(task);
        String message = ((String) response.get("message")).toLowerCase();
        if (message.contains("delivered")) {
            return DeliveryStatus.delivered;
        }
        else if(message.contains("pending")){
            return DeliveryStatus.pending;
        }
        else if(message.contains("invalid")){
            return DeliveryStatus.delivered;
        }
        return DeliveryStatus.undetermined;
    }

    private static Map<String, Object> pollTask(PollingTask task) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<String> response =  restTemplate.exchange(
                    "https://gpcmp.grameenphone.com/ecmapigw/webresources/ecmapigw.v2/",
                    HttpMethod.POST,
                    new HttpEntity<>(
                            new ObjectMapper().writeValueAsString(createPollPayload(task)),
                            new LinkedMultiValueMap<>(Stream.of(
                                    new AbstractMap.SimpleEntry<>("Content-Type", Collections.singletonList("application/json")),
                                    new AbstractMap.SimpleEntry<>("Accept", Collections.singletonList("application/json"))
                            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    ),
                    String.class
            );

            int status = response.getStatusCode().value();
            if (status != 200) {
                throw new HttpStatusCodeException(response.getStatusCode(), Optional.ofNullable(response.getBody()).orElse("client_error")) {};
            }

            return new ObjectMapper().readValue(response.getBody(), new TypeReference<Map<String,Object>>() {});
        } catch (RestClientException | JsonProcessingException e) {
            throw new RestClientException("http_client_exception");
        }
    }

    private static Map<String, Object> createPollPayload(PollingTask task) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "ELAdmin_3338");
        payload.put("password", "Rgl12345^");
        payload.put("apicode", "4");
        payload.put("msisdn", task.terminatingCalledNumber.replaceAll("^88", ""));
        payload.put("countrycode", "0");
        payload.put("cli", "EXCEELI");
        payload.put("messagetype", "3");
        payload.put("message", task.message);
        payload.put("messageid", task.taskId);
        return payload;
    }
}
