package com.xsh.trueused.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;

import com.xsh.trueused.testsupport.IntegrationTestBase;

/**
 * 认证模块集成测试。
 *
 * <p>覆盖完整认证链路：
 * <ol>
 *   <li>用户注册（正常流程 + 参数校验 + 重复用户名）</li>
 *   <li>用户登录（正确凭证 + 错误密码）</li>
 *   <li>JWT 鉴权（携带 token 访问受保护接口 + 无 token 被拒绝）</li>
 * </ol>
 *
 * <p>所有请求通过 MockMvc 发送到真实的 Spring Boot 上下文，
 * 经过 JwtAuthenticationFilter → SecurityConfig → Controller 全链路。
 */
@DisplayName("认证模块集成测试")
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends IntegrationTestBase {

    // ==================== 注册测试 ====================

    @Test
    @DisplayName("注册新用户 - 正常流程应返回用户信息")
    @Order(1)
    void shouldRegisterNewUserSuccessfully() throws Exception {
        String username = "register_test_" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "email", username + "@test.com",
                        "password", "Test123456"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(username + "@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("注册 - 用户名已存在应返回 400")
    @Order(2)
    void shouldRejectDuplicateUsername() throws Exception {
        String username = registerUser("dup_user");

        // 再次注册同名用户
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "email", "another_" + username + "@test.com",
                        "password", "Test123456"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("注册 - 邮箱格式不正确应返回 400")
    @Order(3)
    void shouldRejectInvalidEmailFormat() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "bademail_" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "not-an-email",
                        "password", "Test123456"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("注册 - 密码过短应返回 400")
    @Order(4)
    void shouldRejectShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "shortpw_" + UUID.randomUUID().toString().substring(0, 8),
                        "email", "shortpw@test.com",
                        "password", "12345"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("注册 - 用户名为空应返回 400")
    @Order(5)
    void shouldRejectBlankUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "",
                        "email", "blank@test.com",
                        "password", "Test123456"))))
                .andExpect(status().isBadRequest());
    }

    // ==================== 登录测试 ====================

    @Test
    @DisplayName("登录 - 正确凭证应返回 JWT token")
    @Order(10)
    void shouldLoginWithValidCredentials() throws Exception {
        String username = registerUser("login_ok");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", "Test123456"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.expiresInMs").isNumber())
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("登录 - 错误密码应返回 401")
    @Order(11)
    void shouldRejectWrongPassword() throws Exception {
        String username = registerUser("login_bad");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", "WrongPassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登录 - 不存在的用户应返回 401")
    @Order(12)
    void shouldRejectNonExistentUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "ghost_user_" + UUID.randomUUID(),
                        "password", "Test123456"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登录 - 用户名为空应返回 400")
    @Order(13)
    void shouldRejectBlankUsernameOnLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", "",
                        "password", "Test123456"))))
                .andExpect(status().isBadRequest());
    }

    // ==================== JWT 鉴权测试 ====================

    @Test
    @DisplayName("鉴权 - 携带有效 token 访问受保护接口应返回 200")
    @Order(20)
    void shouldAccessProtectedEndpointWithValidToken() throws Exception {
        String token = registerAndLogin("auth_protected");

        mockMvc.perform(withAuth(get("/api/users/me"), token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @DisplayName("鉴权 - 无 token 访问受保护接口应返回 401")
    @Order(21)
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("鉴权 - 无效 token 访问受保护接口应返回 401")
    @Order(22)
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("鉴权 - 公开接口无需 token 即可访问")
    @Order(23)
    void shouldAccessPublicEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("鉴权 - 非管理员访问管理接口应返回 403")
    @Order(24)
    void shouldRejectNonAdminAccessingAdminEndpoint() throws Exception {
        String token = registerAndLogin("non_admin");

        mockMvc.perform(withAuth(get("/api/admin/users"), token))
                .andExpect(status().isForbidden());
    }
}
