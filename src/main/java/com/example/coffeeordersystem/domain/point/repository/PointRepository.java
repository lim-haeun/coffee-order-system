package com.example.coffeeordersystem.domain.point.repository;

import com.example.coffeeordersystem.domain.point.entity.Point;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PointRepository extends JpaRepository<Point, Long> {
    // 같은 사용자의 잔액 변경 요청이 들어오면 순서대로 처리
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Point> findByUserId(Long userId);
}
