package br.com.microservices.orchestrated.paymentservice.core.consumer;

import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.consumer.group-id}", topics = "${spring.topic.payment-success}")
    public void consumepaymentSuccess (String payload) {
        log.info("Receiving payment-success event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }

    @KafkaListener(groupId = "${spring.consumer.group-id}", topics = "${spring.topic.payment-fail}")
    public void consumepaymentFail (String payload) {
        log.info("Receiving payment-fail event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());
    }
}
