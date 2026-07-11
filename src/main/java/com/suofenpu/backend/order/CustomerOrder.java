package com.suofenpu.backend.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.suofenpu.backend.user.User user;

    @Column(length = 60)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 60)
    private String deliveryTime;

    @Column(nullable = false, length = 40)
    private String invoiceType;

    @Column(length = 500)
    private String orderRemark;

    @Column(nullable = false, length = 40)
    private String couponId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal packingFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal couponDiscount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(
        String orderNo,
        com.suofenpu.backend.user.User user,
        String customerName,
        String phone,
        String address,
        String deliveryTime,
        String invoiceType,
        String orderRemark,
        String couponId,
        BigDecimal subtotal,
        BigDecimal packingFee,
        BigDecimal deliveryFee,
        BigDecimal couponDiscount,
        BigDecimal totalAmount
    ) {
        this.orderNo = orderNo;
        this.user = user;
        this.customerName = customerName;
        this.phone = phone;
        this.address = address;
        this.deliveryTime = deliveryTime;
        this.invoiceType = invoiceType;
        this.orderRemark = orderRemark;
        this.couponId = couponId;
        this.subtotal = subtotal;
        this.packingFee = packingFee;
        this.deliveryFee = deliveryFee;
        this.couponDiscount = couponDiscount;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PAID;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        item.attachTo(this);
        items.add(item);
    }

    public String getOrderNo() {
        return orderNo;
    }

    public com.suofenpu.backend.user.User getUser() {
        return user;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public String getOrderRemark() {
        return orderRemark;
    }

    public String getCouponId() {
        return couponId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getPackingFee() {
        return packingFee;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
