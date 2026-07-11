package com.suofenpu.backend.order;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = """
        订单状态，前后端传字符串枚举：
        PENDING_PAYMENT=待支付；
        PAID=已支付，等待商家接单；
        ACCEPTED=商家已接单；
        PREPARING=制作中；
        DELIVERING=配送中；
        COMPLETED=已完成；
        CANCELLED=已取消；
        REFUNDED=已退款。
        """
)
public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    ACCEPTED,
    PREPARING,
    DELIVERING,
    COMPLETED,
    CANCELLED,
    REFUNDED
}
