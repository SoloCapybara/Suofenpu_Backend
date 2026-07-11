alter table dish_categories
  add column icon_url varchar(500) not null default '/assets/icons/lucide/bowl.svg';

update dish_categories set icon_url = '/assets/icons/lucide/bowl.svg' where code = 'noodles';
update dish_categories set icon_url = '/assets/icons/lucide/sparkle.svg' where code = 'snacks';
update dish_categories set icon_url = '/assets/icons/lucide/cup.svg' where code = 'drinks';
update dish_categories set icon_url = '/assets/icons/lucide/carrot.svg' where code = 'toppings';
update dish_categories set icon_url = '/assets/icons/lucide/gift.svg' where code = 'combo';
