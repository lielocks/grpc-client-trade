package backend.trade.order.repository;

import backend.trade.order.model.Invoice;
import backend.trade.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByOrderDateAndInvoice(LocalDateTime orderDate, Invoice invoice);
}
