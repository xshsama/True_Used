package com.xsh.trueused.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.xsh.trueused.entity.Address;
import com.xsh.trueused.order.dto.ShippingInfoDTO;

class ShippingServiceTest {

    @Test
    void createShippingOrderShouldStartWithPendingPickup() {
        Instant baseTime = Instant.parse("2026-07-06T12:00:00Z");
        ShippingService service = new ShippingService(fixedClock(baseTime));

        ShippingInfoDTO info = service.createShippingOrder(
                "京东物流",
                "JD10001",
                "PLATFORM_OUTBOUND",
                "平台仓",
                "质检仓",
                address());

        assertEquals("PENDING", info.getShippingStatus());
        assertEquals(1, info.getTrackingEvents().size());
        assertEquals("PENDING", info.getTrackingEvents().get(0).getStatus());
        assertEquals(baseTime.plusSeconds(120), info.getEstimatedDeliveryTime());
    }

    @Test
    void reconstructShippingInfoShouldReachDeliveringAfterOneMinute() {
        Instant shippedAt = Instant.parse("2026-07-06T12:00:00Z");
        ShippingService service = new ShippingService(fixedClock(shippedAt.plusSeconds(61)));

        ShippingInfoDTO info = service.reconstructShippingInfo(
                "JD10002",
                "京东物流",
                shippedAt,
                "PLATFORM_OUTBOUND",
                "平台仓",
                "质检仓",
                address());

        assertEquals("DELIVERING", info.getShippingStatus());
        assertEquals(5, info.getTrackingEvents().size());
        assertTrue(info.getTrackingEvents().stream().anyMatch(event -> "PICKED".equals(event.getStatus())));
        assertTrue(info.getTrackingEvents().stream().anyMatch(event -> "DELIVERING".equals(event.getStatus())));
    }

    @Test
    void reconstructShippingInfoShouldReachDeliveredAfterTwoMinutes() {
        Instant shippedAt = Instant.parse("2026-07-06T12:00:00Z");
        ShippingService service = new ShippingService(fixedClock(shippedAt.plusSeconds(121)));

        ShippingInfoDTO info = service.reconstructShippingInfo(
                "SF10003",
                "顺丰速运",
                shippedAt,
                "SELLER_OUTBOUND",
                "上海市",
                "徐汇区",
                address());

        assertEquals("DELIVERED", info.getShippingStatus());
        assertEquals(7, info.getTrackingEvents().size());
        assertEquals("DELIVERED", info.getTrackingEvents().get(info.getTrackingEvents().size() - 1).getStatus());
    }

    @Test
    void refreshShippingInfoShouldCompressLegacyEstimatedDeliveryTime() {
        Instant shippedAt = Instant.parse("2026-07-06T12:00:00Z");
        ShippingService service = new ShippingService(fixedClock(shippedAt.plusSeconds(61)));
        ShippingInfoDTO legacySnapshot = ShippingInfoDTO.builder()
                .shipmentType("PLATFORM_OUTBOUND")
                .trackingNumber("JD10004")
                .expressCompany("京东物流")
                .expressCode("JD")
                .shippingStatus("PENDING")
                .shippedAt(shippedAt)
                .estimatedDeliveryTime(shippedAt.plusSeconds(259200))
                .senderCity("平台仓")
                .senderDistrict("质检仓")
                .receiverCity("上海市")
                .receiverDistrict("浦东新区")
                .trackingEvents(List.of(ShippingInfoDTO.TrackingEvent.builder()
                        .time(shippedAt)
                        .status("PENDING")
                        .description("平台仓已出库，快递员正在揽收中")
                        .location("平台仓质检仓")
                        .build()))
                .build();

        ShippingInfoDTO info = service.refreshShippingInfo(legacySnapshot);

        assertEquals("DELIVERING", info.getShippingStatus());
        assertEquals(shippedAt.plusSeconds(120), info.getEstimatedDeliveryTime());
    }

    private static Clock fixedClock(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private static Address address() {
        Address address = new Address();
        address.setCity("上海市");
        address.setDistrict("浦东新区");
        return address;
    }
}
