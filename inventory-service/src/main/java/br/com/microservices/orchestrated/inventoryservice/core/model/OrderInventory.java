package br.com.microservices.orchestrated.inventoryservice.core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orderInventory")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "inventoryID")
    private Inventory inventory;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private Integer orderQuantity;

    @Column(nullable = false)
    private Integer newQuantity;

    @Column(nullable = false)
    private Integer oldQuantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist () {

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.oldQuantity = inventory.getAmountAvailable();
        this.newQuantity = this.oldQuantity - this.orderQuantity;

    }

    @PreUpdate
    private void preUpdate () {
        this.updatedAt = LocalDateTime.now();
        this.oldQuantity = inventory.getAmountAvailable();
        this.newQuantity = this.oldQuantity - this.orderQuantity;
    }

}
