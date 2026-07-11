package com.suofenpu.backend.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class OrderDtos {
    private OrderDtos() {
    }

    @Schema(description = "提交订单请求")
    public record CreateOrderRequest(
        @Schema(description = "顾客姓名。当前可选，后续接微信登录/地址簿时可自动带出", example = "张三")
        String customerName,
        @Schema(description = "联系电话，用于配送或门店联系", example = "13800008888", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "联系电话不能为空")
        String phone,
        @Schema(description = "收货地址或桌号/取餐信息。外卖填地址，堂食扫码可填桌号", example = "桂林市七星区 漓江路 88 号", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "收货地址不能为空")
        String address,
        @Schema(
            description = "送货/取餐时间。前端当前可传：立即配送、30分钟后、12:00-12:30、18:00-18:30；后端按字符串保存",
            example = "立即配送",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "送货时间不能为空")
        String deliveryTime,
        @Schema(
            description = "发票类型。当前可传：不开发票、个人发票、公司发票；后续如需发票抬头可扩展字段",
            example = "不开发票",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "发票类型不能为空")
        String invoiceType,
        @Schema(description = "整单备注，作用于整个订单，例如配送说明", example = "骑手请放前台")
        String orderRemark,
        @Schema(
            description = "优惠券ID。当前支持：none=不使用；new5=无门槛减5元；full30=满30减5；full50=满50减10。后端会重新判断是否满足门槛",
            example = "new5",
            allowableValues = {"none", "new5", "full30", "full50"}
        )
        String couponId,
        @Schema(description = "订单商品行列表。不同规格/辣度/备注的同菜品应作为不同 item 提交")
        @NotEmpty(message = "订单商品不能为空")
        List<@Valid CreateOrderItemRequest> items
    ) {
    }

    @Schema(description = "提交订单商品行")
    public record CreateOrderItemRequest(
        @Schema(description = "菜品ID，来自 GET /api/menu/dishes 的 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "菜品ID不能为空")
        Long dishId,
        @Schema(
            description = "规格。当前支持：标准份=不加价；加粉=加3元；加臭加料=加5元。后端按该字段计算规格加价",
            example = "标准份",
            allowableValues = {"标准份", "加粉", "加臭加料"},
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "规格不能为空")
        String spec,
        @Schema(
            description = "辣度。当前支持：不辣、微辣、中辣、重辣。只影响后厨制作备注，不影响价格",
            example = "微辣",
            allowableValues = {"不辣", "微辣", "中辣", "重辣"},
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "辣度不能为空")
        String spice,
        @Schema(
            description = "餐具选项。当前支持：需要餐具、不需要餐具。用于门店打包",
            example = "需要餐具",
            allowableValues = {"需要餐具", "不需要餐具"},
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "餐具选项不能为空")
        String tableware,
        @Schema(description = "单品备注，只作用于该商品行，例如不要葱、多酸笋、汤分开", example = "不要葱")
        String itemRemark,
        @Schema(description = "购买数量，必须大于0", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(message = "数量必须大于0")
        int quantity
    ) {
    }

    @Schema(description = "订单详情响应")
    public record OrderResponse(
        @Schema(description = "订单号，后续查询订单和后台改状态使用", example = "SF20260701113926919958")
        String orderNo,
        @Schema(description = "下单用户ID。匿名下单时为空", example = "1")
        Long userId,
        @Schema(description = "顾客姓名", example = "张三")
        String customerName,
        @Schema(description = "联系电话", example = "13800008888")
        String phone,
        @Schema(description = "收货地址或桌号/取餐信息", example = "桂林市七星区 漓江路 88 号")
        String address,
        @Schema(description = "送货/取餐时间", example = "立即配送")
        String deliveryTime,
        @Schema(description = "发票类型", example = "不开发票")
        String invoiceType,
        @Schema(description = "整单备注", example = "骑手请放前台")
        String orderRemark,
        @Schema(description = "实际使用的优惠券ID", example = "new5")
        String couponId,
        @Schema(description = "商品小计，不含打包费/配送费/优惠", example = "33.60")
        BigDecimal subtotal,
        @Schema(description = "打包费，当前规则为每件商品1元", example = "2.00")
        BigDecimal packingFee,
        @Schema(description = "配送费，当前有商品时固定3元", example = "3.00")
        BigDecimal deliveryFee,
        @Schema(description = "优惠券抵扣金额", example = "5.00")
        BigDecimal couponDiscount,
        @Schema(description = "订单应付总额=商品小计+打包费+配送费-优惠券", example = "33.60")
        BigDecimal totalAmount,
        @Schema(description = "订单状态。详见 OrderStatus 枚举说明", example = "PAID")
        OrderStatus status,
        @Schema(description = "订单创建时间，ISO-8601 UTC 时间", example = "2026-07-01T03:39:26.924256Z")
        Instant createdAt,
        @Schema(description = "订单商品明细")
        List<OrderItemResponse> items
    ) {
        public static OrderResponse from(CustomerOrder order) {
            return new OrderResponse(
                order.getOrderNo(),
                order.getUser() == null ? null : order.getUser().getId(),
                order.getCustomerName(),
                order.getPhone(),
                order.getAddress(),
                order.getDeliveryTime(),
                order.getInvoiceType(),
                order.getOrderRemark(),
                order.getCouponId(),
                order.getSubtotal(),
                order.getPackingFee(),
                order.getDeliveryFee(),
                order.getCouponDiscount(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
            );
        }
    }

    @Schema(description = "订单商品明细响应")
    public record OrderItemResponse(
        @Schema(description = "菜品ID", example = "1")
        Long dishId,
        @Schema(description = "下单时的菜品名称快照，避免后续改名影响历史订单", example = "经典螺蛳粉")
        String dishName,
        @Schema(description = "规格", example = "标准份")
        String spec,
        @Schema(description = "辣度", example = "微辣")
        String spice,
        @Schema(description = "餐具选项", example = "需要餐具")
        String tableware,
        @Schema(description = "单品备注", example = "不要葱")
        String itemRemark,
        @Schema(description = "单价，已包含规格加价", example = "16.80")
        BigDecimal unitPrice,
        @Schema(description = "数量", example = "2")
        int quantity,
        @Schema(description = "商品行小计=单价*数量", example = "33.60")
        BigDecimal lineTotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                item.getDishId(),
                item.getDishName(),
                item.getSpec(),
                item.getSpice(),
                item.getTableware(),
                item.getItemRemark(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
            );
        }
    }

    @Schema(description = "后台更新订单状态请求")
    public record UpdateOrderStatusRequest(
        @Schema(
            description = """
                目标订单状态，传字符串枚举：
                PENDING_PAYMENT=待支付；
                PAID=已支付待接单；
                ACCEPTED=已接单；
                PREPARING=制作中；
                DELIVERING=配送中；
                COMPLETED=已完成；
                CANCELLED=已取消；
                REFUNDED=已退款。
                """,
            example = "PREPARING",
            allowableValues = {"PENDING_PAYMENT", "PAID", "ACCEPTED", "PREPARING", "DELIVERING", "COMPLETED", "CANCELLED", "REFUNDED"},
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "订单状态不能为空")
        OrderStatus status
    ) {
    }
}
