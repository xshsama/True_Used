package com.xsh.trueused.interaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xsh.trueused.entity.Favorite;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.interaction.repository.FavoriteRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void addShouldSaveFavoriteAndRefreshProductFavoriteCount() {
        User user = user(100L);
        Product product = product(10L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(favoriteRepository.existsByUserAndProduct(user, product)).thenReturn(false);
        when(favoriteRepository.countByProduct(product)).thenReturn(3L);

        favoriteService.add(10L, 100L);

        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).save(favoriteCaptor.capture());
        Favorite saved = favoriteCaptor.getValue();
        assertSame(user, saved.getUser());
        assertSame(product, saved.getProduct());
        assertEquals(3L, product.getFavoritesCount());
    }

    @Test
    void addShouldBeIdempotentWhenFavoriteAlreadyExists() {
        User user = user(100L);
        Product product = product(10L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(favoriteRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        favoriteService.add(10L, 100L);

        verify(favoriteRepository, never()).save(any());
        verify(favoriteRepository, never()).countByProduct(any());
        assertEquals(0L, product.getFavoritesCount());
    }

    @Test
    void removeShouldDeleteExistingFavoriteAndRefreshProductFavoriteCount() {
        User user = user(100L);
        Product product = product(10L);
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(Optional.of(favorite));
        when(favoriteRepository.countByProduct(product)).thenReturn(2L);

        favoriteService.remove(10L, 100L);

        verify(favoriteRepository).delete(favorite);
        assertEquals(2L, product.getFavoritesCount());
    }

    @Test
    void removeShouldBeIdempotentWhenFavoriteDoesNotExist() {
        User user = user(100L);
        Product product = product(10L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(favoriteRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        favoriteService.remove(10L, 100L);

        verify(favoriteRepository, never()).delete(any());
        verify(favoriteRepository, never()).countByProduct(any());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private static Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setTitle("MacBook Pro");
        product.setDescription("M1 Pro");
        product.setPrice(new BigDecimal("6800.00"));
        return product;
    }
}
