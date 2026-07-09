package com.xsh.trueused.order.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xsh.trueused.entity.Order;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.enums.ProductTradeModel;
import com.xsh.trueused.notification.service.NotificationService;
import com.xsh.trueused.order.enums.OrderStatus;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.order.state.OrderStateMachine;
import com.xsh.trueused.order.state.OrderTransition;
import com.xsh.trueused.product.service.ProductService;
import com.xsh.trueused.wallet.service.WalletService;

@ExtendWith(MockitoExtension.class)
class OrderPaymentStrategyTest {

    @Mock
    private OrderStateMachine orderStateMachine;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private WalletService walletService;

    private DirectOrderPaymentStrategy directStrategy;
    private WalletOrderPaymentStrategy walletStrategy;
    private CallbackOrderPaymentStrategy callbackStrategy;

    @BeforeEach
    void setUp() {
        directStrategy = new DirectOrderPaymentStrategy(
                orderStateMachine,
                orderRepository,
                productService,
                notificationService);
        walletStrategy = new WalletOrderPaymentStrategy(
                orderStateMachine,
                orderRepository,
                productService,
                notificationService,
                walletService);
        callbackStrategy = new CallbackOrderPaymentStrategy(
                orderStateMachine,
                orderRepository,
                productService,
                notificationService);
    }

    @Test
    void directPaymentShouldMoveFreeTradingOrderToPaidAndNotifySeller() {
        Order order = order(900L, ProductTradeModel.FREE_TRADING, OrderStatus.PENDING_PAYMENT);
        when(orderStateMachine.nextStatus(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY))
                .thenReturn(OrderStatus.PAID);

        OrderPaymentResult result = directStrategy.execute(order, OrderPaymentContext.direct(100L));

        assertEquals(OrderPaymentResult.PROCESSED, result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(order.getPaymentTime());
        verify(orderStateMachine).assertCanTransit(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY,
                "Order cannot be paid");
        verify(orderRepository).save(order);
        verify(productService).updateProductStatus(10L, ProductStatus.SOLD);
        verify(notificationService).createNotification(eq(200L), eq("订单已付款"),
                eq("订单 [900] 买家已付款，请尽快发货。"), eq("ORDER_PAID"), eq(900L));
    }

    @Test
    void directPaymentShouldMoveOfficialInspectionOrderToPendingShipment() {
        Order order = order(900L, ProductTradeModel.OFFICIAL_INSPECTION, OrderStatus.PENDING_PAYMENT);

        OrderPaymentResult result = directStrategy.execute(order, OrderPaymentContext.direct(100L));

        assertEquals(OrderPaymentResult.PROCESSED, result);
        assertEquals(OrderStatus.PENDING_SHIPMENT, order.getStatus());
        verify(orderStateMachine, never()).nextStatus(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY);
        verify(notificationService).createNotification(eq(200L), eq("订单已付款"),
                eq("订单 [900] 买家已付款，平台仓将安排出库。"), eq("ORDER_PAID"), eq(900L));
    }

    @Test
    void walletPaymentShouldFreezeBuyerFundsBeforeMarkingOrderPaid() {
        Order order = order(900L, ProductTradeModel.FREE_TRADING, OrderStatus.PENDING_PAYMENT);
        when(orderStateMachine.nextStatus(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY))
                .thenReturn(OrderStatus.PAID);

        OrderPaymentResult result = walletStrategy.execute(order, OrderPaymentContext.wallet(100L, "123456"));

        assertEquals(OrderPaymentResult.PROCESSED, result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(walletService).payOrder(100L, 900L, new BigDecimal("88.00"), "123456");
        verify(orderRepository).save(order);
    }

    @Test
    void callbackPaymentShouldSkipAlreadyPaidOrders() {
        Order order = order(900L, ProductTradeModel.FREE_TRADING, OrderStatus.PAID);

        OrderPaymentResult result = callbackStrategy.execute(order, OrderPaymentContext.callback("trade-1"));

        assertEquals(OrderPaymentResult.SKIPPED, result);
        verify(orderRepository, never()).save(order);
        verify(productService, never()).updateProductStatus(eq(10L), eq(ProductStatus.SOLD));
    }

    @Test
    void callbackPaymentShouldSkipInvalidTransition() {
        Order order = order(900L, ProductTradeModel.FREE_TRADING, OrderStatus.CANCELLED);
        when(orderStateMachine.canTransit(OrderStatus.CANCELLED, OrderTransition.PAY)).thenReturn(false);

        OrderPaymentResult result = callbackStrategy.execute(order, OrderPaymentContext.callback("trade-1"));

        assertEquals(OrderPaymentResult.SKIPPED, result);
        verify(orderRepository, never()).save(order);
    }

    @Test
    void callbackPaymentShouldStoreTransactionAndProcessPayableOrder() {
        Order order = order(900L, ProductTradeModel.FREE_TRADING, OrderStatus.PENDING_PAYMENT);
        when(orderStateMachine.canTransit(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY)).thenReturn(true);
        when(orderStateMachine.nextStatus(OrderStatus.PENDING_PAYMENT, OrderTransition.PAY))
                .thenReturn(OrderStatus.PAID);

        OrderPaymentResult result = callbackStrategy.execute(order, OrderPaymentContext.callback("trade-1"));

        assertEquals(OrderPaymentResult.PROCESSED, result);
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("trade-1", order.getTransactionId());
        assertNotNull(order.getPaymentTime());
        verify(orderRepository).save(order);
        verify(productService).updateProductStatus(10L, ProductStatus.SOLD);
    }

    private static Order order(Long orderId, ProductTradeModel tradeModel, OrderStatus status) {
        User buyer = user(100L);
        User seller = user(200L);
        Product product = new Product();
        product.setId(10L);
        product.setSeller(seller);
        product.setTradeModel(tradeModel);

        Order order = new Order();
        order.setId(orderId);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setProduct(product);
        order.setPrice(new BigDecimal("88.00"));
        order.setStatus(status);
        return order;
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
