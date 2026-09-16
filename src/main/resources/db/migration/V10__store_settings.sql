CREATE TABLE store_settings (
    id BIGINT NOT NULL,
    store_name VARCHAR(120) NOT NULL,
    business_hours VARCHAR(80) NOT NULL,
    delivery_range VARCHAR(120) NOT NULL,
    notice VARCHAR(500) NOT NULL,
    store_open BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO store_settings (
    id,
    store_name,
    business_hours,
    delivery_range,
    notice,
    store_open,
    updated_at
) VALUES (
    1,
    '嗦粉铺桂林七星店',
    '10:00 - 22:00',
    '门店 3 公里内',
    '满 38 减 5，预计 25 分钟送达。',
    TRUE,
    CURRENT_TIMESTAMP
);
