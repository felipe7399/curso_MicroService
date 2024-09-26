package br.com.microservices.orchestrated.orchestratorservice.core.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ESource {

    INVENTORY_SERVICE ("inventory-service"),
    ORCHESTRATOR_SERVICE ("orchestrator-service"),
    ORDER_SERVICE ("order-service"),
    PAYMENT_SERVICE("payment-service"),
    PRODUCT_VALIDATION_SERVICE("product-validation-service");

    private String source;

}
