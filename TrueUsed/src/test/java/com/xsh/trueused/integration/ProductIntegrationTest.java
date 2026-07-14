package com.xsh.trueused.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
 * 商品模块集成测试。
 *
 * <p>覆盖商品完整 CRUD 生命周期及权限校验：
 * <ol>
 *   <li>创建商品（需登录）→ 初始状态 PENDING</li>
 *   <li>公开查询商品详情 & 列表搜索（无需登录）</li>
 *   <li>获取「我的商品」列表（需登录）</li>
 *   <li>更新商品信息</li>
 *   <li>上架 / 下架商品（状态流转）</li>
 *   <li>擦亮商品</li>
 *   <li>删除商品</li>
 *   <li>权限校验：未登录创建 → 401，跨用户操作 → 403</li>
 * </ol>
 */
@DisplayName("商品模块集成测试")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductIntegrationTest extends IntegrationTestBase {

    /** 最小化商品创建请求体 */
    private String buildCreateRequest(String title) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "title", title,
                "description", "集成测试商品描述 - 九成新无划痕",
                "price", new BigDecimal("2999.00"),
                "categoryId", 12,          // 电脑
                "condition", "LIKE_NEW",
                "tradeModel", "FREE_TRADING"
        ));
    }

    // ==================== 创建商品 ====================

    @Test
    @DisplayName("创建商品 - 已登录用户应返回商品信息，状态为 PENDING")
    @Order(1)
    void shouldCreateProductWhenAuthenticated() throws Exception {
        String token = registerAndLogin("seller_create");

        mockMvc.perform(withAuth(post("/api/products"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildCreateRequest("二手 MacBook Pro 2023")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("二手 MacBook Pro 2023"))
                .andExpect(jsonPath("$.price").value(2999.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.condition").value("LIKE_NEW"))
                .andExpect(jsonPath("$.tradeModel").value("FREE_TRADING"));
    }

    @Test
    @DisplayName("创建商品 - 未登录应返回 401")
    @Order(2)
    void shouldRejectCreateWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildCreateRequest("不应创建的商品")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("创建商品 - 标题为空应返回 400")
    @Order(3)
    void shouldRejectCreateWithBlankTitle() throws Exception {
        String token = registerAndLogin("seller_validation");

        mockMvc.perform(withAuth(post("/api/products"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "",
                        "description", "描述",
                        "price", new BigDecimal("100.00")))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建商品 - 价格为负应返回 400")
    @Order(4)
    void shouldRejectCreateWithNegativePrice() throws Exception {
        String token = registerAndLogin("seller_neg_price");

        mockMvc.perform(withAuth(post("/api/products"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "负价格商品",
                        "description", "描述",
                        "price", new BigDecimal("-1.00")))))
                .andExpect(status().isBadRequest());
    }

    // ==================== 查询商品 ====================

    @Test
    @DisplayName("查询商品详情 - 公开访问已上架商品应返回 200")
    @Order(10)
    void shouldGetProductDetailPublicly() throws Exception {
        // 卖家创建并上架商品
        String sellerToken = registerAndLogin("seller_detail");
        Long productId = createAndPublishProduct(sellerToken, "iPhone 15 Pro Max");

        // 无 token 公开访问
        mockMvc.perform(get("/api/products/{id}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.title").value("iPhone 15 Pro Max"))
                .andExpect(jsonPath("$.status").value("ON_SALE"));
    }

    @Test
    @DisplayName("查询商品详情 - 不存在的 ID 应返回 404")
    @Order(11)
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("搜索商品列表 - 关键词搜索应返回匹配结果")
    @Order(12)
    void shouldSearchProductsByKeyword() throws Exception {
        String sellerToken = registerAndLogin("seller_search");
        createAndPublishProduct(sellerToken, "搜索测试关键词_无人机");

        mockMvc.perform(get("/api/products")
                .param("q", "搜索测试关键词")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.title contains '搜索测试关键词')]").exists());
    }

    @Test
    @DisplayName("获取我的商品 - 已登录用户应返回自己的商品列表")
    @Order(13)
    void shouldGetMyProductsWhenAuthenticated() throws Exception {
        String token = registerAndLogin("seller_my");
        createProduct(token, "我的商品_键盘");
        createProduct(token, "我的商品_鼠标");

        mockMvc.perform(withAuth(get("/api/products/my"), token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("获取我的商品 - 未登录应返回 401")
    @Order(14)
    void shouldRejectMyProductsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/products/my"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 更新 / 上架 / 下架 ====================

    @Test
    @DisplayName("更新商品 - 修改标题和价格应返回更新后的信息")
    @Order(20)
    void shouldUpdateProductTitleAndPrice() throws Exception {
        String token = registerAndLogin("seller_update");
        Long productId = createProduct(token, "更新前标题");

        mockMvc.perform(withAuth(put("/api/products/{id}", productId), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "更新后标题",
                        "price", new BigDecimal("1888.00")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新后标题"))
                .andExpect(jsonPath("$.price").value(1888.00));
    }

    @Test
    @DisplayName("上架商品 - PENDING 状态商品上架后应变为 ON_SALE")
    @Order(21)
    void shouldPublishProductToOnSale() throws Exception {
        String token = registerAndLogin("seller_publish");
        Long productId = createProduct(token, "待上架商品_耳机");

        mockMvc.perform(withAuth(put("/api/products/{id}/publish", productId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON_SALE"));
    }

    @Test
    @DisplayName("下架商品 - ON_SALE 状态商品下架后应变为 OFF_SHELF")
    @Order(22)
    void shouldHideProductToOffShelf() throws Exception {
        String token = registerAndLogin("seller_hide");
        Long productId = createAndPublishProduct(token, "待下架商品_相机");

        mockMvc.perform(withAuth(put("/api/products/{id}/hide", productId), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OFF_SHELF"));
    }

    @Test
    @DisplayName("擦亮商品 - 上架中的商品可以擦亮")
    @Order(23)
    void shouldPolishOnSaleProduct() throws Exception {
        String token = registerAndLogin("seller_polish");
        Long productId = createAndPublishProduct(token, "擦亮商品_平板");

        mockMvc.perform(withAuth(put("/api/products/{id}/polish", productId), token))
                .andExpect(status().isOk());
    }

    // ==================== 权限校验 ====================

    @Test
    @DisplayName("权限 - 用户 A 不能修改用户 B 的商品")
    @Order(30)
    void shouldRejectCrossUserProductUpdate() throws Exception {
        String sellerToken = registerAndLogin("seller_a");
        Long productId = createProduct(sellerToken, "用户A的商品");

        String attackerToken = registerAndLogin("attacker_b");

        mockMvc.perform(withAuth(put("/api/products/{id}", productId), attackerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "被篡改的标题"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("权限 - 用户 A 不能删除用户 B 的商品")
    @Order(31)
    void shouldRejectCrossUserProductDelete() throws Exception {
        String sellerToken = registerAndLogin("seller_del_a");
        Long productId = createProduct(sellerToken, "不应被删除的商品");

        String attackerToken = registerAndLogin("attacker_del_b");

        mockMvc.perform(withAuth(delete("/api/products/{id}", productId), attackerToken))
                .andExpect(status().isForbidden());
    }

    // ==================== 删除商品 ====================

    @Test
    @DisplayName("删除商品 - 卖家删除自己的商品应返回 200")
    @Order(40)
    void shouldDeleteOwnProduct() throws Exception {
        String token = registerAndLogin("seller_delete");
        Long productId = createProduct(token, "待删除商品_充电器");

        mockMvc.perform(withAuth(delete("/api/products/{id}", productId), token))
                .andExpect(status().isOk());

        // 删除后查询应返回 404
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    // ==================== 辅助方法 ====================

    /** 创建商品并返回 ID */
    private Long createProduct(String token, String title) throws Exception {
        JsonNode response = parseResponse(mockMvc.perform(withAuth(post("/api/products"), token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildCreateRequest(title)))
                .andExpect(status().isOk())
                .andReturn());
        return response.get("id").asLong();
    }

    /** 创建商品并上架，返回商品 ID */
    private Long createAndPublishProduct(String token, String title) throws Exception {
        Long productId = createProduct(token, title);

        mockMvc.perform(withAuth(put("/api/products/{id}/publish", productId), token))
                .andExpect(status().isOk());

        return productId;
    }
}
