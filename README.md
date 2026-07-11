# 嗦粉铺后端

Spring Boot 单体后端，先覆盖小程序和商家后台联调需要的核心能力。

## 运行

默认使用 MySQL。`root` 有建库权限时，启动会自动创建 `suofenpu` 数据库，然后通过 Flyway 自动建表并插入测试菜品。

```bash
cd backend
docker compose up -d mysql
./gradlew bootRun
```

如果要联调真实微信小程序登录，使用本地环境变量启动脚本。密钥放在 `.env.local`，该文件已被 `.gitignore` 忽略。

```bash
cd backend
./start-local.sh
```

服务地址：`http://localhost:8080`

接口文档：

- Knife4j：`http://localhost:8080/doc.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

默认 MySQL 连接：

- host：`localhost:3306`
- database：`suofenpu`
- username：`root`
- password：`123456`

如果部署到服务器，用环境变量覆盖数据库连接：

```bash
DB_URL='jdbc:mysql://localhost:3306/suofenpu?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
DB_USERNAME=root \
DB_PASSWORD=your_password \
WECHAT_APP_ID=your_wechat_mini_program_app_id \
WECHAT_APP_SECRET=your_wechat_mini_program_app_secret \
WECHAT_DEV_LOGIN_ENABLED=false \
./gradlew bootRun
```

## H2 快速模式

H2 只用于本地快速 smoke test 或自动化测试，不作为正式开发数据库。

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

H2 控制台：`http://localhost:8080/h2-console`

JDBC URL：`jdbc:h2:mem:suofenpu`

## 接口

### 登录/用户

`POST /api/auth/wechat-login`

微信小程序登录/注册。前端先调用 `wx.login` 获取 code，再传给后端。后端用 code 换 openid；用户不存在时自动注册。

```json
{
  "code": "wx.login 返回的 code",
  "nickname": "粉友",
  "avatarUrl": "https://example.com/avatar.png"
}
```

响应里的 `token` 后续放到请求头：

```text
Authorization: Bearer {token}
```

`POST /api/auth/dev-login`

本地联调用手机号登录，不依赖微信 appId/appSecret。

```json
{
  "phone": "13800008888",
  "nickname": "测试用户",
  "avatarUrl": "https://example.com/avatar.png"
}
```

`GET /api/auth/me`

查询当前登录用户。必须带 `Authorization`。

`PATCH /api/auth/me`

更新手机号、昵称、头像。必须带 `Authorization`。

### 客户端

`GET /api/menu/dishes`

返回上架菜品。

`POST /api/orders`

提交订单。后端会重新按菜品、规格、打包费、配送费、优惠券计算金额，并在事务内扣库存。

```json
{
  "customerName": "张三",
  "phone": "13800008888",
  "address": "桂林市七星区 漓江路 88 号",
  "deliveryTime": "立即配送",
  "invoiceType": "不开发票",
  "orderRemark": "骑手请放前台",
  "couponId": "new5",
  "items": [
    {
      "dishId": 1,
      "spec": "标准份",
      "spice": "微辣",
      "tableware": "需要餐具",
      "itemRemark": "不要葱",
      "quantity": 2
    }
  ]
}
```

`GET /api/orders/{orderNo}`

查询订单详情。

`GET /api/orders/my`

查询当前登录用户的订单列表。必须带 `Authorization`。

### 商家后台

`GET /api/admin/orders`

查看最近 100 条订单。

`GET /api/admin/orders?status=PAID`

按状态筛选订单。

`PATCH /api/admin/orders/{orderNo}/status`

```json
{ "status": "PREPARING" }
```

可用状态：`PENDING_PAYMENT`、`PAID`、`ACCEPTED`、`PREPARING`、`DELIVERING`、`COMPLETED`、`CANCELLED`、`REFUNDED`。

`GET /api/admin/dishes`

查看全部菜品。

`PATCH /api/admin/dishes/{id}`

更新价格、库存、上下架。

```json
{
  "price": 18.8,
  "stock": 50,
  "available": true
}
```

## 当前设计边界

- 默认数据库是 MySQL，库存扣减使用数据库行级写锁，几十人同时下单没有问题。
- 支付目前按已支付订单处理，后续接微信支付时增加支付单和支付回调。
- 购物车仍然由小程序本地维护，提交订单时后端只接收最终订单。
- 优惠券先用固定规则，后续可以拆成用户券表。
- 缺货处理暂未加，后续可以在订单上加 `outOfStockPolicy` 字段。
