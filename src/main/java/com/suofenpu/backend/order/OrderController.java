package com.suofenpu.backend.order;

import static com.suofenpu.backend.order.OrderDtos.CreateOrderRequest;
import static com.suofenpu.backend.order.OrderDtos.OrderResponse;

import java.util.List;

import com.suofenpu.backend.user.AuthService;
import com.suofenpu.backend.user.User;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "客户端订单")
public class OrderController {
    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    @Operation(
        summary = "提交订单",
        description = """
            小程序结算页调用。前端提交菜品、规格、辣度、餐具、备注、地址、送货时间、发票、优惠券。
            后端会重新读取菜品价格、计算规格加价、打包费、配送费、优惠券抵扣和应付总额，并在事务内扣减库存。
            当前支付未接微信支付，订单创建后状态直接为 PAID。
            """
    )
    public OrderResponse createOrder(
        @Parameter(description = "可选 Bearer token。登录后传入可把订单关联到当前用户；不传则按匿名订单处理", example = "Bearer 4f2c...")
        @RequestHeader(value = "Authorization", required = false) String authorization,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        User user = authService.resolveUser(authorization).orElse(null);
        return orderService.createOrder(request, user);
    }

    @GetMapping("/my")
    @Operation(summary = "查询当前用户订单列表", description = "小程序订单页使用。必须传 Authorization: Bearer {token}，只返回当前登录用户的订单。")
    public List<OrderResponse> listMyOrders(
        @Parameter(description = "Bearer token，登录接口返回", example = "Bearer 4f2c...")
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return orderService.listUserOrders(authService.requireUser(authorization));
    }

    @GetMapping("/{orderNo}")
    @Operation(
        summary = "查询订单详情",
        description = "小程序订单页调用。根据 orderNo 查询订单、金额、状态和商品明细。"
    )
    public OrderResponse getOrder(
        @Parameter(description = "订单号，来自提交订单响应 orderNo", example = "SF20260701113926919958")
        @PathVariable String orderNo
    ) {
        return orderService.getOrder(orderNo);
    }
}
