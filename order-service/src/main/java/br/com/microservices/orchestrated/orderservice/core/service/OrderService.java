package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.document.Order;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderRequest;
import br.com.microservices.orchestrated.orderservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.orderservice.core.repository.OrderRepository;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import io.swagger.v3.core.util.Json;
import jakarta.validation.Payload;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private OrderRepository repository;
    private JsonUtil jsonUtil;
    private EventService eventService;
    private KafkaProducer kafkaProducer;
    private static final String TRANSACTION_ID_PATTERN = "%S_%S";

    public Order createOrder (OrderRequest orderRequest) {
        Order order = Order.builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now()).transactionId(
                String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli() , UUID.randomUUID())
        ).build();
        repository.save(order);
        kafkaProducer.sendEvent(jsonUtil.toJson(createPayload(order)));
        return order;

    }
    private Event createPayload (Order order) {
        Event event = Event.builder()
                .orderId(order.getId())
                .payload(order)
                .transactionId(order.getTransactionId())
                .createdAt(LocalDateTime.now())
                .build();
        eventService.save(event);
        return event;
    }
}
