package com.xsh.trueused.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xsh.trueused.address.repository.AddressRepository;
import com.xsh.trueused.coupon.repository.UserCouponRepository;
import com.xsh.trueused.entity.Address;
import com.xsh.trueused.entity.Order;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.notification.service.NotificationService;
import com.xsh.trueused.observability.metrics.BusinessMetricsRecorder;
import com.xsh.trueused.order.dto.CreateOrderRequest;
import com.xsh.trueused.order.payment.OrderPaymentStrategyFactory;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.order.state.OrderStateMachine;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.product.service.ProductService;
import com.xsh.trueused.user.repository.UserRepository;
import com.xsh.trueused.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class OrderCommandServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private ShippingService shippingService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductService productService;

    @Mock
    private WalletService walletService;

    @Mock
    private OrderStateMachine orderStateMachine;

    @Mock
    private OrderPaymentStrategyFactory orderPaymentStrategyFactory;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private BusinessMetricsRecorder businessMetricsRecorder;

    @InjectMocks
    private OrderCommandService orderCommandService;

    @Test
    void createOrderShouldRejectBuyingOwnProduct() {
        Product product = product(10L, 100L, ProductStatus.ON_SALE);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.createOrder(createOrderRequest(10L, 20L), 100L));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Buyer cannot purchase own product", ex.getReason());
        verify(userRepository, never()).findById(100L);
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createOrderShouldRejectAddressOwnedByAnotherUser() {
        Product product = product(10L, 200L, ProductStatus.ON_SALE);
        User buyer = user(100L);
        Address address = address(20L, 999L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(100L)).thenReturn(Optional.of(buyer));
        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.createOrder(createOrderRequest(10L, 20L), 100L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("Address does not belong to the current user", ex.getReason());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createOrderShouldRejectWhenProductWasLockedConcurrently() {
        Product product = product(10L, 200L, ProductStatus.ON_SALE);
        User buyer = user(100L);
        Address address = address(20L, 100L);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(100L)).thenReturn(Optional.of(buyer));
        when(addressRepository.findById(20L)).thenReturn(Optional.of(address));
        when(productService.updateProductStatusIfCurrent(10L, ProductStatus.ON_SALE, ProductStatus.LOCKED))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.createOrder(createOrderRequest(10L, 20L), 100L));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Product is not available for purchase", ex.getReason());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void alipayRequestShouldRejectMismatchedAmount() {
        Order order = order(1L, 100L, 200L, new BigDecimal("6800.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.assertAlipayPaymentRequestAllowed(1L, 100L, new BigDecimal("6799.99")));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Payment amount does not match order amount", ex.getReason());
    }

    @Test
    void alipayRequestShouldRejectAnotherBuyer() {
        Order order = order(1L, 100L, 200L, new BigDecimal("6800.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.assertAlipayPaymentRequestAllowed(1L, 999L, new BigDecimal("6800.00")));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to pay for this order", ex.getReason());
    }

    @Test
    void paymentCallbackShouldRejectMismatchedAmount() {
        Order order = order(1L, 100L, 200L, new BigDecimal("6800.00"));
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(order));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderCommandService.handlePaymentSuccess(1L, "trade-1", new BigDecimal("1.00")));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Payment amount does not match order amount", ex.getReason());
    }

    private static CreateOrderRequest createOrderRequest(Long productId, Long addressId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(productId);
        request.setAddressId(addressId);
        return request;
    }

    private static Product product(Long productId, Long sellerId, ProductStatus status) {
        User seller = user(sellerId);

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setTitle("MacBook Pro");
        product.setDescription("M1 Pro");
        product.setPrice(new BigDecimal("6800.00"));
        product.setStatus(status);
        return product;
    }

    private static Address address(Long addressId, Long userId) {
        Address address = new Address();
        address.setId(addressId);
        address.setUser(user(userId));
        return address;
    }

    private static Order order(Long orderId, Long buyerId, Long sellerId, BigDecimal price) {
        Order order = new Order();
        order.setId(orderId);
        order.setBuyer(user(buyerId));
        order.setSeller(user(sellerId));
        order.setPrice(price);
        order.setStatus(com.xsh.trueused.order.enums.OrderStatus.PENDING_PAYMENT);
        return order;
    }

    private static User user(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
