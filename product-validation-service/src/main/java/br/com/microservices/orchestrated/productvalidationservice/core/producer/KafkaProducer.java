package br.com.microservices.orchestrated.productvalidationservice.core.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

private KafkaTemplate<String,String> kafkaTemplate;

@Value("${spring.kafka.topic.orchestrator}")
private String topic;

public void sendEvent (String payload) {

    try{
        log.info("Sending Event to topic {} with data {}", topic , payload);
        kafkaTemplate.send(topic, payload);
    }catch (Exception e){
      log.error("Error sending topic {} containing data {}", topic,payload);
    }

}

}
