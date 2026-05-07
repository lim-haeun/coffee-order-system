package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.point.dto.request.PointChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.response.PointChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.entity.PointHistoryType;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointService(
            UserRepository userRepository,
            PointRepository pointRepository,
            PointHistoryRepository pointHistoryRepository) {
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Transactional
    public PointChargeResponse chargePoint(PointChargeRequest request) {
        validateChargeAmount(request.amount());

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        
        // 포인트가 이미 있으면 비관적 락으로 조회,
        // 없으면 0포인트 상태로 새로 만들고 이번 충전을 적용
          
        Point point = pointRepository.findByUserId(user.getId())
                .orElseGet(() -> new Point(user, 0L));

        point.charge(request.amount());
        Point savedPoint = pointRepository.save(point);

        // 잔액 변경과 충전 이력 저장을 묶어 원자성 보장
        pointHistoryRepository.save(new PointHistory(user, request.amount(), PointHistoryType.CHARGE));

        return new PointChargeResponse(user.getId(), request.amount(), savedPoint.getBalance());
    }

    private void validateChargeAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
    }
}
