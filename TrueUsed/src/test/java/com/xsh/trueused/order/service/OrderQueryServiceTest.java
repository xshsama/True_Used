package com.xsh.trueused.order.service;

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

import com.xsh.trueused.entity.Order;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    void getOrderByIdForUserShouldRejectUnrelatedUser() {
        Order order = new Order();
        order.setId(1L);
        order.setBuyer(user(100L));
        order.setSeller(user(200L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderQueryService.getOrderByIdForUser(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to view this order", ex.getReason());
    }

    @Test
    void getOrderShippingInfoForUserShouldRejectUnrelatedUser() {
        Order order = new Order();
        order.setId(1L);
        order.setBuyer(user(100L));
        order.setSeller(user(200L));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderQueryService.getOrderShippingInfoForUser(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to view this order", ex.getReason());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
