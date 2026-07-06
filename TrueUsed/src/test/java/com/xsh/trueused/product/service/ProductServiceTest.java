package com.xsh.trueused.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsh.trueused.category.repository.CategoryRepository;
import com.xsh.trueused.coupon.repository.UserCouponRepository;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.interaction.repository.FavoriteRepository;
import com.xsh.trueused.notification.service.NotificationService;
import com.xsh.trueused.product.dto.ProductDTO;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserCouponRepository userCouponRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findOneShouldUseDatabaseStatusWhenRedisStatusIsStale() throws Exception {
        ProductDTO cached = new ProductDTO(
                10L,
                "MacBook Pro",
                "M1 Pro",
                new BigDecimal("6800.00"),
                null,
                0.0,
                "CNY",
                ProductStatus.OFF_SHELF,
                null,
                null,
                null,
                null,
                null,
                null,
                "上海市",
                null,
                null,
                0L,
                0L,
                List.of(),
                null,
                null);

        String cachedJson = objectMapper.writeValueAsString(cached);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("product:static:10")).thenReturn(cachedJson);
        when(productRepository.findStatusById(10L)).thenReturn(Optional.of(ProductStatus.ON_SALE));

        ProductDTO result = productService.findOne(10L).orElseThrow();

        assertEquals(ProductStatus.ON_SALE, result.status());
        verify(valueOperations, never()).get("product:status:10");
        verify(valueOperations).set("product:status:10", "ON_SALE");
        verify(valueOperations).set(eq("product:views:10"), eq("0"));
        verify(valueOperations).set(eq("product:favs:10"), eq("0"));
        verify(productRepository, never()).findById(any());
    }
}
