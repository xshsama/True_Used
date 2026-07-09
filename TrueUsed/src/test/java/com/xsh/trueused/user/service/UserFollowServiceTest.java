package com.xsh.trueused.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.entity.User;
import com.xsh.trueused.entity.UserFollow;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.dto.UserFollowDTO;
import com.xsh.trueused.user.repository.UserFollowRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceTest {

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private UserFollowService userFollowService;

    @Test
    void followShouldRejectSelfFollow() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userFollowService.follow(100L, 100L));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Cannot follow yourself", ex.getReason());
        verifyNoInteractions(userRepository, userFollowRepository);
    }

    @Test
    void followShouldCreateRelationWhenMissingAndReturnStatus() {
        User follower = user(100L);
        User followed = user(200L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(200L)).thenReturn(Optional.of(followed));
        when(userFollowRepository.existsByFollower_IdAndFollowed_Id(100L, 200L)).thenReturn(false, true);
        when(userFollowRepository.countByFollowed_Id(200L)).thenReturn(7L);

        UserFollowDTO dto = userFollowService.follow(100L, 200L);

        assertEquals(200L, dto.userId());
        assertEquals(7L, dto.followerCount());
        assertEquals(Boolean.TRUE, dto.following());

        ArgumentCaptor<UserFollow> followCaptor = ArgumentCaptor.forClass(UserFollow.class);
        verify(userFollowRepository).save(followCaptor.capture());
        assertSame(follower, followCaptor.getValue().getFollower());
        assertSame(followed, followCaptor.getValue().getFollowed());
    }

    @Test
    void followShouldBeIdempotentWhenRelationAlreadyExists() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user(100L)));
        when(userRepository.findById(200L)).thenReturn(Optional.of(user(200L)));
        when(userFollowRepository.existsByFollower_IdAndFollowed_Id(100L, 200L)).thenReturn(true);
        when(userFollowRepository.countByFollowed_Id(200L)).thenReturn(7L);

        UserFollowDTO dto = userFollowService.follow(100L, 200L);

        assertEquals(Boolean.TRUE, dto.following());
        verify(userFollowRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unfollowShouldDeleteRelationAndReturnUpdatedStatus() {
        when(userRepository.findById(200L)).thenReturn(Optional.of(user(200L)));
        when(userFollowRepository.countByFollowed_Id(200L)).thenReturn(6L);
        when(userFollowRepository.existsByFollower_IdAndFollowed_Id(100L, 200L)).thenReturn(false);

        UserFollowDTO dto = userFollowService.unfollow(100L, 200L);

        verify(userFollowRepository).deleteByFollower_IdAndFollowed_Id(100L, 200L);
        assertEquals(200L, dto.userId());
        assertEquals(6L, dto.followerCount());
        assertEquals(Boolean.FALSE, dto.following());
    }

    @Test
    void isFollowingShouldReturnFalseForAnonymousOrSelfWithoutQueryingRepository() {
        assertFalse(userFollowService.isFollowing(null, 200L));
        assertFalse(userFollowService.isFollowing(200L, 200L));

        verifyNoInteractions(userFollowRepository);
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
