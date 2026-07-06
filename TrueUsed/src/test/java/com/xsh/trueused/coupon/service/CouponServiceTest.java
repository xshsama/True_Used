package com.xsh.trueused.coupon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.coupon.repository.CouponRepository;
import com.xsh.trueused.coupon.repository.UserCouponRepository;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.entity.UserCoupon;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void useCouponShouldRejectAnotherUserCoupon() {
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setId(1L);
        userCoupon.setUser(user(100L));
        when(userCouponRepository.findById(1L)).thenReturn(Optional.of(userCoupon));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> couponService.useCoupon(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to use this coupon", ex.getReason());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
