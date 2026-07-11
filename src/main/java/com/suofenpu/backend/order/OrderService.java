package com.suofenpu.backend.order;

import static com.suofenpu.backend.order.OrderDtos.CreateOrderRequest;
import static com.suofenpu.backend.order.OrderDtos.OrderResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.suofenpu.backend.common.ApiException;
import com.suofenpu.backend.dish.Dish;
import com.suofenpu.backend.dish.DishRepository;
import com.suofenpu.backend.user.User;

@Service
public class OrderService {
    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final PricingPolicy pricingPolicy;

    public OrderService(DishRepository dishRepository, OrderRepository orderRepository, PricingPolicy pricingPolicy) {
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
        this.pricingPolicy = pricingPolicy;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User user) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (var requestItem : request.items()) {
            Dish dish = dishRepository.findByIdForUpdate(requestItem.dishId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品不存在：" + requestItem.dishId()));

            if (!dish.isAvailable()) {
                throw new ApiException(HttpStatus.CONFLICT, dish.getName() + " 已下架");
            }
            try {
                dish.reduceStock(requestItem.quantity());
            } catch (IllegalStateException exception) {
                throw new ApiException(HttpStatus.CONFLICT, dish.getName() + " 库存不足");
            }

            BigDecimal unitPrice = dish.getPrice().add(pricingPolicy.specPrice(requestItem.spec()));
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(requestItem.quantity()));
            subtotal = subtotal.add(lineTotal);
            itemCount += requestItem.quantity();

            orderItems.add(new OrderItem(
                dish.getId(),
                dish.getName(),
                requestItem.spec(),
                requestItem.spice(),
                requestItem.tableware(),
                requestItem.itemRemark(),
                unitPrice,
                requestItem.quantity(),
                lineTotal
            ));
        }

        BigDecimal packingFee = pricingPolicy.packingFee(itemCount);
        BigDecimal deliveryFee = pricingPolicy.deliveryFee(itemCount);
        BigDecimal beforeDiscountTotal = subtotal.add(packingFee).add(deliveryFee);
        BigDecimal couponDiscount = pricingPolicy.couponDiscount(request.couponId(), subtotal, beforeDiscountTotal);
        BigDecimal totalAmount = beforeDiscountTotal.subtract(couponDiscount);

        CustomerOrder order = new CustomerOrder(
            nextOrderNo(),
            user,
            request.customerName(),
            request.phone(),
            request.address(),
            request.deliveryTime(),
            request.invoiceType(),
            request.orderRemark(),
            request.couponId() == null ? "none" : request.couponId(),
            subtotal,
            packingFee,
            deliveryFee,
            couponDiscount,
            totalAmount
        );
        orderItems.forEach(order::addItem);

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
            .map(OrderResponse::from)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在：" + orderNo));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(OrderStatus status) {
        List<CustomerOrder> orders = status == null
            ? orderRepository.findTop100ByOrderByCreatedAtDesc()
            : orderRepository.findTop100ByStatusOrderByCreatedAtDesc(status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listUserOrders(User user) {
        return orderRepository.findTop100ByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(OrderResponse::from)
            .toList();
    }

    @Transactional
    public OrderResponse updateStatus(String orderNo, OrderStatus status) {
        CustomerOrder order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "订单不存在：" + orderNo));
        order.updateStatus(status);
        return OrderResponse.from(order);
    }

    private String nextOrderNo() {
        long suffix = System.nanoTime() % 1000;
        return "SF" + LocalDateTime.now().format(ORDER_NO_TIME) + String.format("%03d", suffix);
    }
}
