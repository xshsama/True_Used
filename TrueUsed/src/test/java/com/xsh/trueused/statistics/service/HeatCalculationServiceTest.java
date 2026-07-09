package com.xsh.trueused.statistics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xsh.trueused.entity.Product;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.interaction.repository.CommentRepository;
import com.xsh.trueused.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class HeatCalculationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private HeatCalculationService heatCalculationService;

    @Test
    void calculateAndSaveHeatShouldApplyViewsFavoritesCommentsDiscountAndTimeDecay() {
        Product product = product(10L, 100L, 2L, "100.00", "50.00", Duration.ofHours(2).plusSeconds(5));
        when(commentRepository.countByProduct(product)).thenReturn(4L);

        heatCalculationService.calculateAndSaveHeat(product);

        assertEquals(26.25, product.getHeatScore(), 0.001);
        verify(productRepository).save(product);
    }

    @Test
    void updateAllProductHeatsShouldOnlyLoadOnSaleProducts() {
        Product product = product(10L, 20L, 1L, "100.00", "80.00", Duration.ofHours(1));
        when(productRepository.findByStatus(ProductStatus.ON_SALE)).thenReturn(List.of(product));
        when(commentRepository.countByProduct(product)).thenReturn(0L);

        heatCalculationService.updateAllProductHeats();

        verify(productRepository).findByStatus(ProductStatus.ON_SALE);
        verify(productRepository).save(product);
    }

    private static Product product(Long id, Long views, Long favorites, String originalPrice, String price,
            Duration age) {
        Product product = new Product();
        product.setId(id);
        product.setViewsCount(views);
        product.setFavoritesCount(favorites);
        product.setOriginalPrice(new BigDecimal(originalPrice));
        product.setPrice(new BigDecimal(price));
        product.setCreatedAt(Instant.now().minus(age));
        return product;
    }
}
