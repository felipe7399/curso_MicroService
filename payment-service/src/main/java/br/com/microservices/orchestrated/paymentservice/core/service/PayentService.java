package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.config.exceptions.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.Payment;
import br.com.microservices.orchestrated.paymentservice.core.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
    private final JsonUtil jsonUtil;
    private final PaymentRepository paymentRepository;
    private final KafkaProducer kafkaProducer;


    public void performPayment (Event event) {

        try{
            checkCurrentPayment(event);
            createPendingPayment(event);
        } catch (Exception e){
            log.error("Error to process payment: ".concat(e.getMessage()));
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    private void createPendingPayment(Event event) {

        var totalAmount= event.getPayload().getProducts().stream()
                .map(orderProducts -> orderProducts.getQuantity() * orderProducts.getProduct().getUnitValue())
                .reduce(0.00, Double::sum);
        var totalItems= event.getPayload().getProducts().stream()
                .map(OrderProducts::getQuantity)
                .reduce(0, Integer::sum);

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();

        paymentRepository.save(payment);
        event.getPayload().setTotalItems(totalItems);
        event.getPayload().setTotalAmount(totalAmount);


    }

    private void checkCurrentPayment(Event event) {

        if (paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There is already a payment being processed with the same ID");
        }

    }
}
