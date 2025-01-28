package br.com.microservices.orchestrated.paymentservice.core.trpository;

import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);

}


