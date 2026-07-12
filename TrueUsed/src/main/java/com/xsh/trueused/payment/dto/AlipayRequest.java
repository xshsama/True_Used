package com.xsh.trueused.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AlipayRequest {
    @NotBlank(message = "订单号不能为空")
    private String outTradeNo;
    @NotBlank(message = "支付金额不能为空")
    private String totalAmount;
    @NotBlank(message = "支付标题不能为空")
    private String subject;
    private String body;
}
