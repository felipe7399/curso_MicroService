package br.com.microservices.orchestrated.productvalidationservice.core.service;

import br.com.microservices.orchestrated.productvalidationservice.config.exceptions.ValidationException;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.History;
import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Validation;
import br.com.microservices.orchestrated.productvalidationservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repository.ValidationRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ProductValidationService {

    private static final String  CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

    private final ProductRepository productRepository;
    private final JsonUtil jsonUtil;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ValidationRepository validationRepository;
    private final KafkaProducer kafkaProducer;


    public void validateExistingProducts (Event event) {

       try{
           checkCurrentValidation(event);
           createValidation(event, true);
           handleWithSuccess(event);
       }catch (Exception ex){
           log.error("Error to validate products");
           handleWithFail(event, ex.getMessage());
       }
       kafkaProducer.sendEvent(jsonUtil.toJson(event));


    }

    private void handleWithFail(Event event, String message) {

        var history = History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.ROLLBACK_PENDING)
                .message("Fail to validate products: ".concat(message))
                .createdAt(LocalDateTime.now())
                .build();

        event.setSource(CURRENT_SOURCE);
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.addHistory(history);
    }

    private void handleWithSuccess(Event event) {

        var history = History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .message("Products validated successfully")
                .build();

        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        event.addHistory(history);

    }

    private void createValidation(Event event,boolean success) {

        Validation validation = Validation
                .builder()
                .transactionId(event.getTransactionId())
                .orderId(event.getPayload().getId())
                .success(success)
                .build();
        validationRepository.save(validation);

    }

    private void checkCurrentValidation(Event event) {

        if (event.getPayload() == null || event.getPayload().getProducts() == null) {
            throw new ValidationException("Empty product list");
        }

        if (event.getPayload().getId() == null || event.getPayload().getTransactionId() == null) {
            throw new ValidationException("OrderId and TransactionId must be informed");

        }
        if (validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("an order with the same OrderId/TransactionId is running");
        }
        event.getPayload().getProducts().forEach(product -> {
            if(product == null || product.getProduct().getCode() == null){
                throw new ValidationException("Product is empty or code is empty");
            }
            if (!productRepository.existsByCode(product.getProduct().getCode())){
                throw new ValidationException("Product does not exist in database ");
            }

        });

        }

        public void rollbackEvent (Event event) {

            validationRepository.findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                    .ifPresentOrElse(validation -> {
                                validation.setSuccess(false);
                                validationRepository.save(validation);
                            },
                            () -> createValidation(event, false));

            var history = History.builder()
                    .source(CURRENT_SOURCE)
                    .status(ESagaStatus.FAIL)
                    .createdAt(LocalDateTime.now())
                    .message("Rollback executed on product validation")
                    .build();

        event.setStatus(ESagaStatus.FAIL);
        event.setSource(CURRENT_SOURCE);
        kafkaProducer.sendEvent(jsonUtil.toJson(event));

        }
}
