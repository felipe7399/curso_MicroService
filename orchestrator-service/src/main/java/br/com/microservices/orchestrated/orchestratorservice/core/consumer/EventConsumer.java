package br.com.microservices.orchestrated.orchestratorservice.core.consumer;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.start-saga}" )
    public void consumeStartSaga (String payload) {
        log.info("Receiving start-saga event{}" , payload);
        Event event = jsonUtil.toEvent(payload);
        //var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());

    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.orchestrator}" )
    public void consumeOrchestrator (String payload) {
        log.info("Receiving orchestrator event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());

    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.finish-success}" )
    public void consumeFinishSuccess (String payload) {
        log.info("Receiving FinishSuccess event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());

    }

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}", topics = "${spring.kafka.topic.finish-fail}" )
    public void consumeFinishFail (String payload) {
        log.info("Receiving FinishFail event{}" , payload);
        //Event event = jsonUtil.toEvent(payload);
        var event = jsonUtil.toEvent(payload);
        log.info("evento {} ", event.toString());

    }
}
