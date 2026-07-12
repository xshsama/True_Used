package com.xsh.trueused.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.xsh.trueused.api.ApiResponse;
import com.xsh.trueused.config.AlipayConfiguration;
import com.xsh.trueused.order.service.OrderCommandService;
import com.xsh.trueused.payment.dto.AlipayFormResponse;
import com.xsh.trueused.payment.dto.AlipayRequest;
import com.xsh.trueused.payment.service.AlipayService;
import com.xsh.trueused.security.user.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class AlipayControllerTest {

    @Mock
    private AlipayService alipayService;

    @Mock
    private AlipayConfiguration alipayConfiguration;

    @Mock
    private OrderCommandService orderCommandService;

    @InjectMocks
    private AlipayController alipayController;

    @Test
    void payShouldReturnWrappedHtmlForm() throws Exception {
        UserPrincipal principal = principal(100L);
        AlipayRequest request = request("9001", "6899.00");
        when(alipayService.createPayment(any())).thenReturn("<form action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do\"></form>");

        ResponseEntity<ApiResponse<AlipayFormResponse>> response = alipayController.pay(request, principal);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().getCode());
        assertEquals("<form action=\"https://openapi-sandbox.dl.alipaydev.com/gateway.do\"></form>",
                response.getBody().getData().htmlForm());
        verify(orderCommandService).assertAlipayPaymentRequestAllowed(9001L, 100L, new BigDecimal("6899.00"));
        verify(alipayService).createPayment(eq(request));
    }

    @Test
    void payShouldRejectInvalidAmountFormat() {
        UserPrincipal principal = principal(100L);
        AlipayRequest request = request("9001", "bad-amount");

        assertThrows(IllegalArgumentException.class, () -> alipayController.pay(request, principal));
    }

    private static AlipayRequest request(String outTradeNo, String totalAmount) {
        AlipayRequest request = new AlipayRequest();
        request.setOutTradeNo(outTradeNo);
        request.setTotalAmount(totalAmount);
        request.setSubject("TrueUsed Order 9001");
        request.setBody("Body");
        return request;
    }

    private static UserPrincipal principal(Long id) {
        return new UserPrincipal(id, "buyer", "buyer@example.com", "secret", true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
