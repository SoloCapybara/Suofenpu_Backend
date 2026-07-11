insert into dishes
  (name, description, short_description, price, image_url, stock, available, version, created_at, updated_at)
values
  ('经典螺蛳粉', '卤水、锅烧、酸豆角、花生、葱花，默认微辣。', '传统配方，经典美味', 16.80, '/assets/banner-1.jpg', 100, true, 0, current_timestamp, current_timestamp),
  ('加辣螺蛳粉', '鲜汤打底，酸笋加量，辣油更香，适合重口味。', '加辣加臭，过瘾够味', 17.80, '/assets/banner-1.jpg', 100, true, 0, current_timestamp, current_timestamp),
  ('鸭脚螺蛳粉', '酸笋、木耳、腐竹、辣椒油，配软糯鸭脚。', '软糯鸭脚，越啃越香', 22.80, '/assets/banner-1.jpg', 80, true, 0, current_timestamp, current_timestamp),
  ('秘制酸笋', '门店秘制酸笋，适合加料或单点小菜。', '爽脆酸笋，开胃加料', 6.80, '/assets/banner-1.jpg', 120, true, 0, current_timestamp, current_timestamp);
