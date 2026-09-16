CREATE TABLE home_banners (
    id BIGINT NOT NULL AUTO_INCREMENT,
    image_url VARCHAR(500) NOT NULL,
    title VARCHAR(80) NOT NULL,
    highlight VARCHAR(80) NOT NULL,
    badge VARCHAR(120) NOT NULL,
    sort_order INT NOT NULL,
    enabled BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO home_banners (
    image_url,
    title,
    highlight,
    badge,
    sort_order,
    enabled,
    updated_at
) VALUES
(
    '/assets/banner-1.jpg',
    '一碗好粉',
    '嗦出柳州味',
    '地道柳州螺蛳粉 · 匠心熬制',
    1,
    TRUE,
    CURRENT_TIMESTAMP
),
(
    '/assets/banner-2.jpg',
    '酸辣鲜香',
    '现煮更过瘾',
    '酸笋加量 · 辣油飘香',
    2,
    TRUE,
    CURRENT_TIMESTAMP
),
(
    '/assets/banner-3.jpg',
    '下单免排队',
    '到店即取',
    '套餐小食 · 热粉同享',
    3,
    TRUE,
    CURRENT_TIMESTAMP
);
