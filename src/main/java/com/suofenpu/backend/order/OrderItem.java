package com.suofenpu.backend.order;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    @Column(nullable = false)
    private Long dishId;

    @Column(nullable = false, length = 80)
    private String dishName;

    @Column(nullable = false, length = 60)
    private String spec;

    @Column(nullable = false, length = 40)
    private String spice;

    @Column(nullable = false, length = 40)
    private String tableware;

    @Column(length = 500)
    private String itemRemark;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {
    }

    public OrderItem(
        Long dishId,
        String dishName,
        String spec,
        String spice,
        String tableware,
        String itemRemark,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
    ) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.spec = spec;
        this.spice = spice;
        this.tableware = tableware;
        this.itemRemark = itemRemark;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    void attachTo(CustomerOrder order) {
        this.order = order;
    }

    public Long getDishId() {
        return dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public String getSpec() {
        return spec;
    }

    public String getSpice() {
        return spice;
    }

    public String getTableware() {
        return tableware;
    }

    public String getItemRemark() {
        return itemRemark;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
