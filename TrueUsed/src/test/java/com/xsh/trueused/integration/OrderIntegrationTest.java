package com.xsh.trueused.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.xsh.trueused.testsupport.IntegrationTestBase;

/**
 * 订单模块集成测试。
 *
 * <p>覆盖二手交易完整订单生命周期及异常场景：
 * <ol>
 *   <li>【完整流程】卖家发布商品 → 买家创建地址 → 下单 → 支付 → 发货 → 确认收货</li>
 *   <li>买家查看「我的订单」、卖家查看「已卖出订单」</li>
 *   <li>取消订单（待付款阶段）</li>
 *   <li>买家不能购买自己的商品</li>
 *   <li>非订单参与人无法查看订单</li>
 *   <li>快递公司列表查询</li>
 * </ol>
 */
@DisplayName("订单模块集成测试")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderIntegrationTest extends IntegrationTestBase {

    // ==================== 完整订单生命周期 ====================

    @Test
    @DisplayName("完整流程：发布 → 下单 → 支付 → 发货 → 确认收货")
    @Order(1)
    void shouldCompleteFullOrderLifecycle() throws Exception {
        // ---- 1. 卖家注册、创建并上架商品 ----
        String sellerToken = registerAndLogin("seller_order_full");
        Long productId = createAndPublishProduct(sellerToken, "订单测试_二手Switch");

        // ---- 2. 买家注册、创建收货地址 ----
        String buyerToken = registerAndLogin("buyer_order_full");
        Long addressId = createAddress(buyerToken);

        // ---- 3. 买家下单 ----
        Long orderId = createOrder(buyerToken, productId, addressId);
        verifyOrderStatus(orderId, buyerToken, "PENDING_PAYMENT");

        // ---- 4. 买家支付 ----
        mockMvc.perform(withAuth(put("/api/orders/{id}/pay", orderId), buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        // 支付后订单状态应为 PAID 或 PENDING_SHIPMENT
        String statusAfterPay = getOrderStatus(orderId, buyerToken);
        assertTrue(statusAfterPay.equals("PAID") || statusAfterPay.equals("PENDING_SHIPMENT"),
                "支付后订单状态应为 PAID 或 PENDING_SHIPMENT，实际: " + statusAfterPay);

        // ---- 5. 卖家发货 ----
        mockMvc.perform(withAuth(put("/api/orders/{id}/ship", orderId), sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "expressCompany", "顺丰速运",
                        "trackingNumber", "SF" + System.currentTimeMillis()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.trackingNumber").isNotEmpty())
                .andExpect(jsonPath("$.expressCompany").value("顺丰速运"));

        // ---- 6. 买家确认收货 ----
        mockMvc.perform(withAuth(put("/api/orders/{id}/confirm-delivery", orderId), buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // ==================== 订单查询 ====================

    @Test
    @DisplayName("买家查看「我的订单」应包含已购买的订单")
    @Order(10)
    void shouldGetMyOrdersAsBuyer() throws Exception {
        String sellerToken = registerAndLogin("seller_myorders");
        Long productId = createAndPublishProduct(sellerToken, "我的订单测试_PS5");

        String buyerToken = registerAndLogin("buyer_myorders");
        Long addressId = createAddress(buyerToken);
        Long orderId = createOrder(buyerToken, productId, addressId);

        mockMvc.perform(withAuth(get("/api/orders/my-orders"), buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + orderId + ")]").exists());
    }

    @Test
    @DisplayName("卖家查看「已卖出订单」应包含已售出的订单")
    @Order(11)
    void shouldGetSoldOrdersAsSeller() throws Exception {
        String sellerToken = registerAndLogin("seller_soldorders");
        Long productId = createAndPublishProduct(sellerToken, "卖出订单测试_iPad");

        String buyerToken = registerAndLogin("buyer_soldorders");
        Long addressId = createAddress(buyerToken);
        createOrder(buyerToken, productId, addressId);

        mockMvc.perform(withAuth(get("/api/orders/sold-orders"), sellerToken)
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("查看订单详情 - 买家和卖家均可查看")
    @Order(12)
    void shouldGetOrderDetailForBothParties() throws Exception {
        String sellerToken = registerAndLogin("seller_detail_o");
        Long productId = createAndPublishProduct(sellerToken, "订单详情测试_Watch");

        String buyerToken = registerAndLogin("buyer_detail_o");
        Long addressId = createAddress(buyerToken);
        Long orderId = createOrder(buyerToken, productId, addressId);

        // 买家查看
        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.product.id").value(productId));

        // 卖家查看
        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    // ==================== 取消订单 ====================

    @Test
    @DisplayName("取消订单 - 待付款阶段买家可取消")
    @Order(20)
    void shouldCancelOrderInPendingPayment() throws Exception {
        String sellerToken = registerAndLogin("seller_cancel");
        Long productId = createAndPublishProduct(sellerToken, "取消订单测试_键盘");

        String buyerToken = registerAndLogin("buyer_cancel");
        Long addressId = createAddress(buyerToken);
        Long orderId = createOrder(buyerToken, productId, addressId);

        mockMvc.perform(withAuth(put("/api/orders/{id}/cancel", orderId), buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("异常 - 买家不能购买自己的商品")
    @Order(30)
    void shouldRejectBuyOwnProduct() throws Exception {
        String sellerToken = registerAndLogin("seller_selfbuy");
        Long productId = createAndPublishProduct(sellerToken, "自购测试_显示器");
        Long addressId = createAddress(sellerToken);

        mockMvc.perform(withAuth(post("/api/orders"), sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "addressId", addressId))))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("异常 - 非订单参与人无法查看订单")
    @Order(31)
    void shouldRejectNonParticipantAccessingOrder() throws Exception {
        String sellerToken = registerAndLogin("seller_excl");
        Long productId = createAndPublishProduct(sellerToken, "排除测试_路由器");

        String buyerToken = registerAndLogin("buyer_excl");
        Long addressId = createAddress(buyerToken);
        Long orderId = createOrder(buyerToken, productId, addressId);

        // 第三方用户尝试查看订单
        String strangerToken = registerAndLogin("stranger_excl");

        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), strangerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("异常 - 未登录用户无法创建订单")
    @Order(32)
    void shouldRejectCreateOrderWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "productId", 1,
                        "addressId", 1))))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 物流查询 ====================

    @Test
    @DisplayName("查询快递公司列表 - 应返回支持的快递公司")
    @Order(40)
    void shouldGetExpressCompanies() throws Exception {
        String token = registerAndLogin("buyer_express");

        mockMvc.perform(withAuth(get("/api/orders/express-companies"), token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("查询物流追踪 - 已发货订单应包含物流信息")
    @Order(41)
    void shouldGetShippingInfoForShippedOrder() throws Exception {
        String sellerToken = registerAndLogin("seller_ship_info");
        Long productId = createAndPublishProduct(sellerToken, "物流测试_音箱");

        String buyerToken = registerAndLogin("buyer_ship_info");
        Long addressId = createAddress(buyerToken);
        Long orderId = createOrder(buyerToken, productId, addressId);

        // 支付
        mockMvc.perform(withAuth(put("/api/orders/{id}/pay", orderId), buyerToken))
                .andExpect(status().isOk());

        // 发货
        mockMvc.perform(withAuth(put("/api/orders/{id}/ship", orderId), sellerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "expressCompany", "中通快递",
                        "trackingNumber", "ZT" + System.currentTimeMillis()))))
                .andExpect(status().isOk());

        // 查询物流
        mockMvc.perform(withAuth(get("/api/orders/{id}/shipping", orderId), buyerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").isNotEmpty())
                .andExpect(jsonPath("$.expressCompany").value("中通快递"));
    }

    // ==================== 辅助方法 ====================

    /** 创建商品并上架，返回商品 ID */
    private Long createAndPublishProduct(String token, String title) throws Exception {
        // 创建商品
        JsonNode createResp = parseResponse(mockMvc.perform(withAuth(post("/api/products"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "description", "订单集成测试商品",
                        "price", new BigDecimal("599.00"),
                        "categoryId", 12,
                        "condition", "GOOD",
                        "tradeModel", "FREE_TRADING"))))
                .andExpect(status().isOk())
                .andReturn());
        Long productId = createResp.get("id").asLong();

        // 上架
        mockMvc.perform(withAuth(put("/api/products/{id}/publish", productId), token))
                .andExpect(status().isOk());

        return productId;
    }

    /** 创建收货地址，返回地址 ID */
    private Long createAddress(String token) throws Exception {
        JsonNode resp = parseResponse(mockMvc.perform(withAuth(post("/api/addresses"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "recipientName", "测试收件人",
                        "phone", "13800138000",
                        "province", "广东省",
                        "city", "深圳市",
                        "district", "南山区",
                        "detailedAddress", "科技园路1号",
                        "isDefault", true))))
                .andExpect(status().isOk())
                .andReturn());
        return resp.get("id").asLong();
    }

    /** 创建订单，返回订单 ID */
    private Long createOrder(String buyerToken, Long productId, Long addressId) throws Exception {
        JsonNode resp = parseResponse(mockMvc.perform(withAuth(post("/api/orders"), buyerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "productId", productId,
                        "addressId", addressId))))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn());
        return resp.get("id").asLong();
    }

    /** 查询订单状态 */
    private String getOrderStatus(Long orderId, String token) throws Exception {
        JsonNode resp = parseResponse(mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), token))
                .andExpect(status().isOk())
                .andReturn());
        return resp.get("status").asText();
    }

    /** 断言订单状态 */
    private void verifyOrderStatus(Long orderId, String token, String expectedStatus) throws Exception {
        mockMvc.perform(withAuth(get("/api/orders/{id}", orderId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expectedStatus));
    }
}
