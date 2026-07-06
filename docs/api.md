# TrueUsed API

本文档记录当前接口口径，作为前后端联调和验收基线。所有普通 JSON 接口都由后端统一包装为标准响应。

## 标准响应

```json
{
  "code": 0,
  "message": "OK",
  "data": {},
  "timestamp": 1710000000000
}
```

约定：

- `code = 0` 表示成功。
- 非 `0` 表示业务错误或 HTTP 错误。
- 文件、PDF、字节流接口不包标准响应。
- 前端 `src/utils/request.js` 会自动解包 `data`。

## 认证

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 注册 | 公开 |
| POST | `/api/auth/login` | 登录 | 公开 |
| POST | `/api/auth/refresh` | 刷新 access token | 公开，依赖 HttpOnly Cookie |
| POST | `/api/auth/logout` | 登出并撤销 token | 公开入口，携带 token 时撤销 |

认证头：

```text
Authorization: Bearer <access_token>
```

## 商品与分类

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/products` | 商品列表与搜索 | 公开 |
| GET | `/api/products/{id}` | 商品详情 | 公开 |
| GET | `/api/products/my` | 我的商品 | 登录 |
| POST | `/api/products` | 发布商品 | 登录 |
| PUT | `/api/products/{id}` | 修改商品 | 登录，卖家本人 |
| DELETE | `/api/products/{id}` | 删除商品 | 登录，卖家本人 |
| PUT | `/api/products/{id}/publish` | 上架商品 | 登录，卖家本人 |
| PUT | `/api/products/{id}/hide` | 隐藏商品 | 登录，卖家本人 |
| PUT | `/api/products/{id}/polish` | 擦亮商品 | 登录，卖家本人 |
| GET | `/api/categories` | 分类列表 | 公开 |

## 订单

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/orders` | 创建订单 | 登录 |
| GET | `/api/orders` | 我的订单列表 | 登录 |
| GET | `/api/orders/{id}` | 订单详情 | 登录，买家或卖家 |
| PUT | `/api/orders/{id}/pay` | 直接支付 | 登录，买家本人 |
| PUT | `/api/orders/{id}/pay-wallet` | 钱包支付 | 登录，买家本人 |
| PUT | `/api/orders/{id}/ship` | 卖家发货 | 登录，卖家本人 |
| PUT | `/api/orders/{id}/confirm` | 确认收货 | 登录，买家本人 |

订单状态以 [后端状态流转文档](/Users/xshsama/code/TrueUsed/TrueUsed/后端状态流转.md) 为准。

## 钱包

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/wallet` | 钱包信息 | 登录 |
| POST | `/api/wallet/set-password` | 设置支付密码 | 登录 |
| POST | `/api/wallet/top-up` | mock 充值 | 登录 |
| POST | `/api/wallet/withdraw` | 提现申请 | 登录 |
| GET | `/api/wallet/transactions` | 钱包流水 | 登录 |

## 售后退款

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/refunds` | 发起退款 | 登录，买家本人 |
| GET | `/api/refunds/{id}` | 退款详情 | 登录，买家或卖家 |
| PUT | `/api/refunds/{id}/approve` | 卖家同意退款 | 登录，卖家本人 |
| PUT | `/api/refunds/{id}/reject` | 卖家拒绝退款 | 登录，卖家本人 |
| PUT | `/api/refunds/{id}/complete-return` | mock 完成退货退款 | 登录，卖家本人 |

## 验货与物流

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/inspections` | 验货报告列表 | 登录 |
| GET | `/api/inspections/{id}` | 验货报告详情 | 登录 |
| GET | `/api/orders/{id}` | 订单详情中包含物流快照 | 登录，买家或卖家 |

当前验货与物流属于演示闭环：物流节点由系统 mock 推进，不接第三方物流平台。

## 文件与第三方

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/api/cloudinary/**` | 图片上传相关 | 登录 |
| POST | `/api/alipay/notify` | 支付宝异步回调 | 公开，内部校验 |

## 错误处理

常见错误：

| HTTP 状态 | 场景 |
| --- | --- |
| 400 | 参数错误、状态非法 |
| 401 | 未登录或 token 无效 |
| 403 | 无资源权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突，例如商品已锁定、优惠券已使用 |
| 500 | 未预期服务端异常 |
