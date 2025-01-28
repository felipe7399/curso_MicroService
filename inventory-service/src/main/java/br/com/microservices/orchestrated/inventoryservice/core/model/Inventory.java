package br.com.microservices.orchestrated.inventoryservice.core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "inventory")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @Column(nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Integer amountAvailable;


}
