package br.com.microservices.orchestrated.inventoryservice.core.consumer;

import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.inventory-success}")
    public void consumeInventorySuccess (String payload) {
        log.info("Receiving inventory-success event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.inventory-fail}")
    public void consumeInventoryFail (String payload) {
        log.info("Receiving inventory-fail event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }
}
