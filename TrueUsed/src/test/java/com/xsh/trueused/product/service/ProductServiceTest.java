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
import com.xsh.trueused.entity.Favorite;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.coupon.repository.UserCouponRepository;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.interaction.repository.FavoriteRepository;
import com.xsh.trueused.notification.service.NotificationService;
import com.xsh.trueused.product.dto.ProductDTO;
import com.xsh.trueused.product.dto.ProductUpdateRequest;
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

    @Test
    void updateShouldNotifyFavoriteUsersWhenPriceDropsAndEvictStaticCache() {
        Product product = product(10L, 100L, ProductStatus.ON_SALE, "6800.00");
        Favorite favorite = new Favorite();
        favorite.setUser(user(200L));
        favorite.setProduct(product);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(favoriteRepository.findByProduct(product)).thenReturn(List.of(favorite));

        ProductUpdateRequest request = new ProductUpdateRequest(
                null,
                null,
                new BigDecimal("6500.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        ProductDTO result = productService.update(10L, request, 100L);

        assertEquals(0, new BigDecimal("6500.00").compareTo(result.price()));
        verify(notificationService).createNotification(
                200L,
                "降价提醒",
                "您收藏的宝贝“MacBook Pro”降价了！从 ¥6800.00 降至 ¥6500.00",
                "PRICE_DROP",
                10L);
        verify(redisTemplate).delete("product:static:10");
    }

    @Test
    void updateProductStatusIfCurrentShouldRefreshStatusCacheOnlyWhenUpdated() {
        when(productRepository.updateStatusIfCurrent(10L, ProductStatus.ON_SALE, ProductStatus.LOCKED))
                .thenReturn(1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        boolean updated = productService.updateProductStatusIfCurrent(10L, ProductStatus.ON_SALE, ProductStatus.LOCKED);

        assertEquals(true, updated);
        verify(valueOperations).set("product:status:10", "LOCKED");
        verify(redisTemplate).delete("product:static:10");
    }

    @Test
    void updateProductStatusIfCurrentShouldReturnFalseWhenConcurrentUpdateWins() {
        when(productRepository.updateStatusIfCurrent(10L, ProductStatus.ON_SALE, ProductStatus.LOCKED))
                .thenReturn(0);

        boolean updated = productService.updateProductStatusIfCurrent(10L, ProductStatus.ON_SALE, ProductStatus.LOCKED);

        assertEquals(false, updated);
        verify(redisTemplate, never()).opsForValue();
        verify(redisTemplate, never()).delete(org.mockito.ArgumentMatchers.anyString());
    }

    private static Product product(Long productId, Long sellerId, ProductStatus status, String price) {
        Product product = new Product();
        product.setId(productId);
        product.setSeller(user(sellerId));
        product.setTitle("MacBook Pro");
        product.setDescription("M1 Pro");
        product.setPrice(new BigDecimal(price));
        product.setStatus(status);
        return product;
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
