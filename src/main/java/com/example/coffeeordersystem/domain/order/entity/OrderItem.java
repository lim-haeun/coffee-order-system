package com.example.coffeeordersystem.domain.order.entity;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private String menuName;

    @Column(nullable = false)
    private Long menuPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    /*
     * 주문 당시의 메뉴명과 가격을 함께 저장해 둔다.
     * 메뉴 정보가 나중에 바뀌어도 과거 주문 금액과 인기 메뉴 집계 기준이 흔들리지 않는다.
     */
    public OrderItem(Order order, Menu menu, int quantity) {
        this.order = order;
        this.menu = menu;
        this.menuName = menu.getName();
        this.menuPrice = Long.valueOf(menu.getPrice());
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Menu getMenu() {
        return menu;
    }

    public String getMenuName() {
        return menuName;
    }

    public Long getMenuPrice() {
        return menuPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
