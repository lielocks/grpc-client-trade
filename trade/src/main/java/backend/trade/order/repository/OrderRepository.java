package backend.trade.order.repository;

import backend.trade.order.model.Invoice;
import backend.trade.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(Long userId);
//    Page<Order> findByOrderDateAndInvoice(LocalDateTime orderDate, Invoice invoice, Pageable pageable);
    Optional<Order> findByOrderDateAndInvoice(LocalDateTime orderDate, Invoice invoice);
}
