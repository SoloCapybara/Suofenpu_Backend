create table admin_users (
  id bigint not null auto_increment,
  username varchar(64) not null,
  password_hash varchar(100) not null,
  display_name varchar(80) not null,
  role varchar(30) not null,
  enabled boolean not null,
  created_at timestamp not null,
  updated_at timestamp not null,
  primary key (id),
  constraint uk_admin_users_username unique (username)
);

create table admin_sessions (
  id bigint not null auto_increment,
  token varchar(128) not null,
  admin_user_id bigint not null,
  expires_at timestamp not null,
  created_at timestamp not null,
  primary key (id),
  constraint uk_admin_sessions_token unique (token),
  constraint fk_admin_sessions_user foreign key (admin_user_id) references admin_users (id) on delete cascade
);

create index idx_admin_sessions_user_id on admin_sessions (admin_user_id);
create index idx_admin_sessions_expires_at on admin_sessions (expires_at);
