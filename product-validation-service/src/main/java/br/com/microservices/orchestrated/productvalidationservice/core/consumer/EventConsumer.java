package br.com.microservices.orchestrated.productvalidationservice.core.consumer;

import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.consumer.group-id}", topics = "${spring.topic.product-validation-success}")
    public void consumeproductValidationSuccess (String payload) {
        log.info("Receiving product-validation-success event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }

    @KafkaListener(groupId = "${spring.consumer.group-id}", topics = "${spring.topic.product-validation-fail}")
    public void consumeproductValidationFail (String payload) {
        log.info("Receiving product-validation-fail event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }
}
