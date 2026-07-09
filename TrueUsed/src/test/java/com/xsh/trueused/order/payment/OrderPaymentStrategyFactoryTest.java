package com.xsh.trueused.order.payment;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrderPaymentStrategyFactoryTest {

    @Test
    void getShouldReturnStrategyRegisteredForType() {
        OrderPaymentStrategy direct = mock(OrderPaymentStrategy.class);
        OrderPaymentStrategy wallet = mock(OrderPaymentStrategy.class);
        when(direct.type()).thenReturn(OrderPaymentType.DIRECT);
        when(wallet.type()).thenReturn(OrderPaymentType.WALLET);

        OrderPaymentStrategyFactory factory = new OrderPaymentStrategyFactory(List.of(direct, wallet));

        assertSame(wallet, factory.get(OrderPaymentType.WALLET));
    }

    @Test
    void getShouldFailFastWhenStrategyIsMissing() {
        OrderPaymentStrategy direct = mock(OrderPaymentStrategy.class);
        when(direct.type()).thenReturn(OrderPaymentType.DIRECT);
        OrderPaymentStrategyFactory factory = new OrderPaymentStrategyFactory(List.of(direct));

        assertThrows(IllegalStateException.class, () -> factory.get(OrderPaymentType.CALLBACK));
    }
}
