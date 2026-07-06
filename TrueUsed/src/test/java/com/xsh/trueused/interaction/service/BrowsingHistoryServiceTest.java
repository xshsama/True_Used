package com.xsh.trueused.interaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;

import com.xsh.trueused.entity.BrowsingHistory;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.interaction.dto.BrowsingHistoryDTO;
import com.xsh.trueused.interaction.repository.BrowsingHistoryRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BrowsingHistoryServiceTest {

    @Mock
    private BrowsingHistoryRepository browsingHistoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BrowsingHistoryService browsingHistoryService;

    @Test
    void recordHistoryShouldIgnoreOwnProduct() {
        Product product = product(10L, user(100L), ProductStatus.ON_SALE);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        browsingHistoryService.recordHistory(100L, 10L);

        verify(userRepository, never()).findById(100L);
        verify(browsingHistoryRepository, never()).findByUserIdAndProductId(100L, 10L);
        verify(browsingHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getUserHistoryShouldExcludeOwnAndUnavailableProductsAtRepositoryLevel() {
        Pageable pageable = PageRequest.of(0, 20);
        BrowsingHistory history = new BrowsingHistory();
        history.setId(1L);
        history.setProduct(product(20L, user(200L), ProductStatus.ON_SALE));
        history.setViewedAt(Instant.parse("2026-07-06T12:00:00Z"));

        when(browsingHistoryRepository.findVisibleByUserId(100L, ProductStatus.ON_SALE, pageable))
                .thenReturn(new PageImpl<>(List.of(history), pageable, 1));

        Streamable<BrowsingHistoryDTO> result = browsingHistoryService.getUserHistory(100L, pageable);

        List<BrowsingHistoryDTO> items = result.toList();
        assertEquals(1, items.size());
        assertEquals(20L, items.get(0).product().id());
        verify(browsingHistoryRepository, never()).findByUserIdOrderByViewedAtDesc(100L, pageable);
    }

    private static Product product(Long productId, User seller, ProductStatus status) {
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setTitle("Product " + productId);
        product.setDescription("description");
        product.setPrice(new BigDecimal("11.00"));
        product.setStatus(status);
        return product;
    }

    private static User user(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("user-" + userId);
        user.setEmail("user-" + userId + "@trueused.test");
        return user;
    }
}
