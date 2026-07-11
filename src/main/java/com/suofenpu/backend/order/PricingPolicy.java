package com.suofenpu.backend.order;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PricingPolicy {
    private static final BigDecimal PACKING_FEE_PER_ITEM = new BigDecimal("1.00");
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

    private static final Map<String, BigDecimal> SPEC_PRICE = Map.of(
        "标准份", new BigDecimal("0.00"),
        "加粉", new BigDecimal("3.00"),
        "加臭加料", new BigDecimal("5.00")
    );

    private static final Map<String, CouponRule> COUPONS = Map.of(
        "new5", new CouponRule(BigDecimal.ZERO, new BigDecimal("5.00")),
        "full30", new CouponRule(new BigDecimal("30.00"), new BigDecimal("5.00")),
        "full50", new CouponRule(new BigDecimal("50.00"), new BigDecimal("10.00")),
        "none", new CouponRule(BigDecimal.ZERO, BigDecimal.ZERO)
    );

    public BigDecimal specPrice(String spec) {
        return SPEC_PRICE.getOrDefault(spec, BigDecimal.ZERO);
    }

    public BigDecimal packingFee(int itemCount) {
        return PACKING_FEE_PER_ITEM.multiply(BigDecimal.valueOf(itemCount));
    }

    public BigDecimal deliveryFee(int itemCount) {
        return itemCount > 0 ? DELIVERY_FEE : BigDecimal.ZERO;
    }

    public BigDecimal couponDiscount(String couponId, BigDecimal subtotal, BigDecimal beforeDiscountTotal) {
        CouponRule rule = COUPONS.getOrDefault(couponId == null ? "none" : couponId, COUPONS.get("none"));
        if (subtotal.compareTo(rule.minSpend()) < 0) {
            return BigDecimal.ZERO;
        }
        return rule.discount().min(beforeDiscountTotal);
    }

    private record CouponRule(BigDecimal minSpend, BigDecimal discount) {
    }
}
