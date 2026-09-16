-- The original default display name was decoded as ISO-8859-1 by a properties
-- reader and then persisted as UTF-8. Repair only that exact corrupted value;
-- names changed by administrators are left untouched.
update admin_users
set display_name = '店长'
where username = 'admin'
  and display_name = 'åºé¿';
