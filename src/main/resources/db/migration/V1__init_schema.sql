create table dishes (
  id bigint not null auto_increment,
  name varchar(80) not null,
  description varchar(500) not null,
  short_description varchar(160) not null,
  price decimal(10, 2) not null,
  image_url varchar(255) not null,
  stock int not null,
  available boolean not null,
  version bigint not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  primary key (id)
);

create table orders (
  id bigint not null auto_increment,
  order_no varchar(32) not null,
  customer_name varchar(60),
  phone varchar(30) not null,
  address varchar(255) not null,
  delivery_time varchar(60) not null,
  invoice_type varchar(40) not null,
  order_remark varchar(500),
  coupon_id varchar(40) not null,
  subtotal decimal(10, 2) not null,
  packing_fee decimal(10, 2) not null,
  delivery_fee decimal(10, 2) not null,
  coupon_discount decimal(10, 2) not null,
  total_amount decimal(10, 2) not null,
  status varchar(30) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  primary key (id),
  constraint uk_orders_order_no unique (order_no)
);

create table order_items (
  id bigint not null auto_increment,
  order_id bigint not null,
  dish_id bigint not null,
  dish_name varchar(80) not null,
  spec varchar(60) not null,
  spice varchar(40) not null,
  tableware varchar(40) not null,
  item_remark varchar(500),
  unit_price decimal(10, 2) not null,
  quantity int not null,
  line_total decimal(10, 2) not null,
  primary key (id),
  constraint fk_order_items_order foreign key (order_id) references orders (id)
);

create index idx_orders_status_created_at on orders (status, created_at);
create index idx_order_items_dish_id on order_items (dish_id);
