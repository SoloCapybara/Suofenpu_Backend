create table users (
  id bigint not null auto_increment,
  openid varchar(128),
  unionid varchar(128),
  phone varchar(30),
  nickname varchar(80),
  avatar_url varchar(500),
  status varchar(30) not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  primary key (id),
  constraint uk_users_openid unique (openid),
  constraint uk_users_phone unique (phone)
);

create table user_sessions (
  id bigint not null auto_increment,
  token varchar(128) not null,
  user_id bigint not null,
  expires_at timestamp not null,
  created_at timestamp not null,
  primary key (id),
  constraint uk_user_sessions_token unique (token),
  constraint fk_user_sessions_user foreign key (user_id) references users (id)
);

alter table orders add column user_id bigint;
alter table orders add constraint fk_orders_user foreign key (user_id) references users (id);

create index idx_user_sessions_user_id on user_sessions (user_id);
create index idx_user_sessions_expires_at on user_sessions (expires_at);
create index idx_orders_user_created_at on orders (user_id, created_at);
