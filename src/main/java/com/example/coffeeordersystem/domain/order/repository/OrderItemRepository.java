package com.example.coffeeordersystem.domain.order.repository;

import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select oi.menu.id as menuId,
                   oi.menuName as name,
                   oi.menuPrice as price,
                   count(oi.id) as orderCount
            from OrderItem oi
            where oi.order.orderedAt >= :from
              and oi.order.status = :status
            group by oi.menu.id, oi.menuName, oi.menuPrice
            order by count(oi.id) desc, oi.menu.id asc
            """)
    List<PopularMenuProjection> findPopularMenus(
            @Param("from") LocalDateTime from,
            @Param("status") OrderStatus status,
            Pageable pageable
    );
}
