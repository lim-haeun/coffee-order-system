package com.example.coffeeordersystem.domain.point.entity;

import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "points")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Long balance;

    protected Point() {
    }

    public Point(User user, Long balance) {
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (balance == null || balance < 0) {
            throw new BusinessException(ErrorCode.INVALID_POINT_BALANCE);
        }

        this.user = user;
        this.balance = balance;
    }

    public void charge(Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        this.balance += amount;
    }

    public void use(Long amount) {
        if (amount == null || amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        if (this.balance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        this.balance -= amount;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Long getBalance() {
        return balance;
    }
}
