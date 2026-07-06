package com.xsh.trueused.inspection.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.consignment.repository.ConsignmentRepository;
import com.xsh.trueused.entity.Consignment;
import com.xsh.trueused.entity.Inspection;
import com.xsh.trueused.entity.Order;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.enums.ConsignmentStatus;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.inspection.repository.InspectionItemRepository;
import com.xsh.trueused.inspection.repository.InspectionRepository;
import com.xsh.trueused.inspection.repository.InspectionResultRepository;
import com.xsh.trueused.notification.service.NotificationService;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.product.service.ProductService;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionItemRepository inspectionItemRepository;

    @Mock
    private InspectionResultRepository inspectionResultRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ConsignmentRepository consignmentRepository;

    @Mock
    private ProductService productService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private InspectionService inspectionService;

    @Test
    void getInspectionByIdForUserShouldRejectUnrelatedUser() {
        Inspection inspection = new Inspection();
        inspection.setId(1L);
        inspection.setOrder(order(100L, 200L));
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> inspectionService.getInspectionByIdForUser(1L, 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to view this inspection", ex.getReason());
    }

    @Test
    void handleConsignmentInspectionFailureShouldUpdateLinkedProductById() {
        Consignment consignment = new Consignment();
        consignment.setId(10L);
        when(consignmentRepository.findById(10L)).thenReturn(Optional.of(consignment));
        when(consignmentRepository.findProductIdById(10L)).thenReturn(Optional.of(20L));

        inspectionService.handleConsignmentInspectionFailure(10L);

        assertEquals(ConsignmentStatus.REJECTED, consignment.getStatus());
        verify(productService).updateInspectionGrade(20L, "X");
        verify(productService).updateProductStatus(20L, ProductStatus.OFF_SHELF);
    }

    private static Order order(Long buyerId, Long sellerId) {
        Order order = new Order();
        order.setBuyer(user(buyerId));
        order.setSeller(user(sellerId));
        return order;
    }

    private static User user(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }
}
