create table dish_categories (
  id bigint not null auto_increment,
  code varchar(40) not null,
  name varchar(40) not null,
  sort_order int not null,
  enabled boolean not null,
  version bigint not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  primary key (id),
  constraint uk_dish_categories_code unique (code)
);

insert into dish_categories
  (code, name, sort_order, enabled, version, created_at, updated_at)
values
  ('noodles', '螺蛳粉', 1, true, 0, current_timestamp, current_timestamp),
  ('snacks', '特色小吃', 2, true, 0, current_timestamp, current_timestamp),
  ('drinks', '饮品甜品', 3, true, 0, current_timestamp, current_timestamp),
  ('toppings', '加料配菜', 4, true, 0, current_timestamp, current_timestamp),
  ('combo', '套餐组合', 5, true, 0, current_timestamp, current_timestamp);

alter table dishes add column category_id bigint;

update dishes
set category_id = (select id from dish_categories where code = 'noodles')
where name like '%螺蛳粉%';

update dishes
set category_id = (select id from dish_categories where code = 'toppings')
where category_id is null and (name like '%鸭脚%' or name like '%卤蛋%' or name like '%腐竹%' or name like '%木耳%' or name like '%花生%');

update dishes
set category_id = (select id from dish_categories where code = 'snacks')
where category_id is null;

alter table dishes modify category_id bigint not null;
alter table dishes add constraint fk_dishes_category foreign key (category_id) references dish_categories (id);
create index idx_dishes_category_available on dishes (category_id, available);
