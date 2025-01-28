package br.com.microservices.orchestrated.paymentservice.core.service;

import br.com.microservices.orchestrated.paymentservice.config.exceptions.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.trpository.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";

    private static final double MINIMUM_AMOUNT = 0.1;
    private final JsonUtil jsonUtil;
    private final PaymentRepository paymentRepository;
    private final KafkaProducer kafkaProducer;


    public void performPayment (Event event) {

        try{
            checkCurrentPayment(event);
            createPendingPayment(event);
            Payment payment = paymentRepository.findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                    .orElseThrow(() -> new ValidationException("Payment not found in DB"));
            validateAmount(payment.getTotalAmount());
            payment.setPaymentStatus(EPaymentStatus.APPROVED);
            paymentRepository.save(payment);
            handleSuccess(event);
        } catch (Exception e){
            log.error("Error to process payment: ".concat(e.getMessage()));
            handleFailure(event);
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));
    }

    public void performRollback (Event event) {
        paymentRepository.findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())
                .ifPresentOrElse(payment -> {
                            payment.setPaymentStatus(EPaymentStatus.DENIED);
                            paymentRepository.save(payment);
                        },
                        () -> createDeniedPayment(event) );
        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        event.addHistory(History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.FAIL)
                .createdAt(LocalDateTime.now())
                .message("Rollback executed")
                .build());
        kafkaProducer.sendEvent(jsonUtil.toJson(event));

    }

    private void createDeniedPayment(Event event) {

        Payment payment = Payment.builder()
                .paymentStatus(EPaymentStatus.DENIED)
                .totalItems(event.getPayload().getTotalItems())
                .totalAmount(event.getPayload().getTotalAmount())
                .transactionId(event.getTransactionId())
                .orderId(event.getOrderId())
                .build();
        paymentRepository.save(payment);

    }

    private void handleFailure(Event event) {

        History history = History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.ROLLBACK_PENDING)
                .message("Payment pending rollback")
                .createdAt(LocalDateTime.now())
                .build();


        event.setSource(CURRENT_SOURCE);
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.addHistory(history);
    }

    private void handleSuccess(Event event) {

        History history = History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.SUCCESS)
                .message("Payment approved")
                .createdAt(LocalDateTime.now())
                .build();

        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        event.addHistory(history);

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

    private void validateAmount (double amount) {

        if (amount < MINIMUM_AMOUNT) {
            throw new ValidationException("Amount is shorter than the minimum amount allowed");
        }

    }
}
