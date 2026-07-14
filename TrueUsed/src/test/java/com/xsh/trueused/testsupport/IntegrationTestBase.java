package com.xsh.trueused.testsupport;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * 集成测试基类。
 *
 * <p>使用 Testcontainers 启动真实的 MySQL 8 和 Redis 7 容器，
 * 通过 {@code @ServiceConnection} 让 Spring Boot 自动发现并注入连接参数，
 * Flyway 会在容器启动后自动执行全部迁移脚本（建表 + 种子数据）。
 *
 * <p><b>前置条件：</b>运行环境需安装 Docker（或兼容的容器运行时）。
 *
 * <p>子类只需继承本类并编写 {@code @Test} 方法即可，
 * 容器为 static 字段，整个测试套件期间只启动一次。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    /** MySQL 8.0 容器，Flyway 迁移在此容器上执行 */
    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("trueused")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    /** Redis 7 容器，用于 TokenRevocationService 等缓存依赖 */
    @Container
    @ServiceConnection
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ==================== 辅助方法 ====================

    /**
     * 注册新用户并自动登录，返回 JWT access token。
     *
     * @param prefix 用户名前缀，用于区分不同测试方法
     * @return JWT access token 字符串
     */
    protected String registerAndLogin(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@test.com";
        String password = "Test123456";

        // 注册
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "email", email,
                        "password", password))))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status != 201) {
                        throw new AssertionError("注册失败, status=" + status
                                + ", body=" + result.getResponse().getContentAsString());
                    }
                });

        // 登录
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password))))
                .andReturn();

        return extractToken(loginResult);
    }

    /**
     * 仅注册新用户（不登录），返回用户名。
     */
    protected String registerUser(String prefix) throws Exception {
        String username = prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@test.com";
        String password = "Test123456";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "email", email,
                        "password", password))));

        return username;
    }

    /**
     * 用指定用户名/密码登录，返回 JWT access token。
     */
    protected String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "username", username,
                        "password", password))))
                .andReturn();
        return extractToken(result);
    }

    /**
     * 从登录响应中提取 token 字段。
     */
    protected String extractToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(body);
        return root.get("token").asText();
    }

    /**
     * 从 MvcResult 响应中提取 JSON 节点。
     */
    protected JsonNode parseResponse(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    /**
     * 构造带 Bearer token 的请求构建器。
     */
    protected MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder, String token) {
        return builder.header("Authorization", "Bearer " + token);
    }
}
