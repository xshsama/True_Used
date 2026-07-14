package com.xsh.trueused.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsh.trueused.address.repository.AddressRepository;
import com.xsh.trueused.auth.repository.RoleRepository;
import com.xsh.trueused.entity.Address;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.Role;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.ProductCondition;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.enums.ProductTradeModel;
import com.xsh.trueused.enums.RoleName;
import com.xsh.trueused.enums.UserStatus;
import com.xsh.trueused.order.enums.OrderStatus;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
class CoreBusinessFlowIntegrationTest {

    private static final String PASSWORD = "Passw0rd!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void configureExternalCacheAdapter() {
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(setOperations.pop(anyString(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(List.of());
    }

    @Test
    void registerThenLoginReturnsUsableJwt() throws Exception {
        ensureUserRole();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "integration-user",
                                  "email": "integration-user@example.com",
                                  "password": "Passw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("integration-user"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));

        String token = login("integration-user");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/orders/my-orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void buyerCreatesOrderAndProductIsLocked() throws Exception {
        Role userRole = ensureUserRole();
        User seller = createUser("integration-seller", userRole);
        User buyer = createUser("integration-buyer", userRole);
        Address address = createAddress(buyer);
        Product product = createOnSaleProduct(seller);
        String buyerToken = login(buyer.getUsername());

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "addressId": %d
                                }
                                """.formatted(product.getId(), address.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.price").value(2999.00))
                .andExpect(jsonPath("$.data.buyer.username").value(buyer.getUsername()))
                .andExpect(jsonPath("$.data.seller.username").value(seller.getUsername()));

        assertThat(orderRepository.findAll())
                .singleElement()
                .satisfies(order -> {
                    assertThat(order.getBuyer().getId()).isEqualTo(buyer.getId());
                    assertThat(order.getSeller().getId()).isEqualTo(seller.getId());
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
                });
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStatus())
                .isEqualTo(ProductStatus.LOCKED);
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    private Role ensureUserRole() {
        return roleRepository.findByName(RoleName.ROLE_USER).orElseGet(() -> {
            Role role = new Role();
            role.setName(RoleName.ROLE_USER);
            role.setDescription("Integration test user");
            return roleRepository.save(role);
        });
    }

    private User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    private Address createAddress(User buyer) {
        Address address = new Address();
        address.setUser(buyer);
        address.setRecipientName("Integration Buyer");
        address.setPhone("13800138000");
        address.setProvince("Zhejiang");
        address.setCity("Hangzhou");
        address.setDistrict("Xihu");
        address.setDetailedAddress("No. 1 Integration Road");
        address.setAreaCode("330106");
        address.setIsDefault(true);
        return addressRepository.save(address);
    }

    private Product createOnSaleProduct(User seller) {
        Product product = new Product();
        product.setSeller(seller);
        product.setTitle("Integration Test Laptop");
        product.setDescription("A product persisted for the complete order flow");
        product.setPrice(new BigDecimal("2999.00"));
        product.setOriginalPrice(new BigDecimal("4999.00"));
        product.setCondition(ProductCondition.GOOD);
        product.setTradeModel(ProductTradeModel.FREE_TRADING);
        product.setStatus(ProductStatus.ON_SALE);
        return productRepository.save(product);
    }
}
