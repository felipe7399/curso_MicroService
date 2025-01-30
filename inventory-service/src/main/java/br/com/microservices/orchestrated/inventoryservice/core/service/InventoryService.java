package br.com.microservices.orchestrated.inventoryservice.core.service;


import br.com.microservices.orchestrated.inventoryservice.config.exceptions.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.History;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import br.com.microservices.orchestrated.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repository.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY SERVICE";
    private final JsonUtil jsonUtil;
    private final KafkaProducer kafkaProducer;
    private final OrderInventoryRepository orderInventoryRepository;
    private final InventoryRepository inventoryRepository;

    public void updateInventory (Event event) {
        
        try {
            checkCurrentValidation(event);
            createOrderInventory(event);
            updateInventory(event.getPayload());        
            handleWithSuccess(event);            
        } catch (Exception ex) {
            log.error("Error trying to update inventory : ", ex);
            handleWithFailure(event, ex.getMessage());
        }
        kafkaProducer.sendEvent(jsonUtil.toJson(event));

    }

    public void rollbackInventory(Event event) {
        var history = History.builder()
                .message("Rollback executed")
                .createdAt(LocalDateTime.now())
                .status(ESagaStatus.FAIL)
                .source(CURRENT_SOURCE)
                .build();

        try {
            undoInventory(event);
        } catch (Exception ex) {
            history.setMessage("Rollback not executed");
        }
        event.setSource(CURRENT_SOURCE);
        event.setStatus(ESagaStatus.FAIL);
        event.addHistory(history);
    }

    private void undoInventory(Event event) {

        var orderInventoryList = orderInventoryRepository
                .findByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId());
        orderInventoryList.forEach(orderInventory -> {
            orderInventory.getInventory().setAmountAvailable(orderInventory.getOldQuantity());
            inventoryRepository.save(  orderInventory.getInventory());
            log.info("Restored inventory for order {} from {} to {}", event.getPayload().getId(), orderInventory.getNewQuantity(), orderInventory.getInventory().getAmountAvailable());

        });

    }

    private void handleWithSuccess(Event event) {
        var history = History.builder()
                .message("Inventory updated correctly")
                .createdAt(LocalDateTime.now())
                .status(ESagaStatus.SUCCESS)
                .source(CURRENT_SOURCE)
                .build();


        event.setStatus(ESagaStatus.SUCCESS);
        event.setSource(CURRENT_SOURCE);
        event.addHistory(history);
    }

    private void updateInventory(Order payload) {

        payload.getProducts().forEach(orderProducts -> {
            var inventory = inventoryRepository.findByProductCodeOrderByID(orderProducts.getProduct().getCode())
                    .orElseThrow(() -> new ValidationException("inventory not found for id : " + orderProducts.getProduct().getCode()));
            if (inventory.getAmountAvailable() < orderProducts.getQuantity()){
                throw new ValidationException("Order quantity is less than existing quantity");
            }
            inventory.setAmountAvailable(inventory.getAmountAvailable() - orderProducts.getQuantity());
            inventoryRepository.save(inventory);
        });
    }

    private void createOrderInventory(Event event) {

        event.getPayload().getProducts().forEach(orderProducts -> {
           var inventory = inventoryRepository.findByProductCodeOrderByID(orderProducts.getProduct().getCode())
                    .orElseThrow(() -> new ValidationException("Product not found by code:  " + orderProducts.getProduct().getCode()));
           var orderInventory = createOrderInventory(event, orderProducts ,inventory);
           orderInventoryRepository.save(orderInventory);
        });
    }

    private OrderInventory createOrderInventory(Event event, OrderProducts orderProducts, Inventory inventory) {

        return OrderInventory.builder()
                .inventory(inventory)
                .orderQuantity(orderProducts.getQuantity())
                .orderId(event.getOrderId())
                .transactionId(event.getTransactionId())
                .build();
    }

    private void checkCurrentValidation(Event event) {

        if (orderInventoryRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())){
            throw new ValidationException("There is already a transaction ID for this event");
        }

    }

    private void handleWithFailure(Event event, String message) {
        History history = History.builder()
                .source(CURRENT_SOURCE)
                .status(ESagaStatus.ROLLBACK_PENDING)
                .message("Inventory pending rollback")
                .createdAt(LocalDateTime.now())
                .build();


        event.setSource(CURRENT_SOURCE);
        event.setStatus(ESagaStatus.ROLLBACK_PENDING);
        event.addHistory(history);
    }


}
