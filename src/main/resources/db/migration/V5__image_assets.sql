CREATE TABLE image_assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_image_assets_sort ON image_assets (sort_order, id);

INSERT INTO image_assets (name, image_url, sort_order, enabled) VALUES
('经典螺蛳粉主图', '/assets/banner-1.jpg', 1, TRUE),
('加辣螺蛳粉主图', '/assets/banner-2.jpg', 2, TRUE),
('门店点餐宣传图', '/assets/banner-3.jpg', 3, TRUE),
('菜单默认螺蛳粉图', '/assets/rice-noodle-hero.png', 4, TRUE);
