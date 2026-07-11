package com.suofenpu.backend.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByOrderNo(String orderNo);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findTop100ByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findTop100ByStatusOrderByCreatedAtDesc(OrderStatus status);

    @EntityGraph(attributePaths = "items")
    List<CustomerOrder> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
}
