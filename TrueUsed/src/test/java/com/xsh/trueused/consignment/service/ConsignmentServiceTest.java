package com.xsh.trueused.consignment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.category.repository.CategoryRepository;
import com.xsh.trueused.consignment.repository.ConsignmentRepository;
import com.xsh.trueused.entity.Consignment;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.inspection.service.InspectionService;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ConsignmentServiceTest {

    @Mock
    private ConsignmentRepository consignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InspectionService inspectionService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ConsignmentService consignmentService;

    @Test
    void updateLogisticsShouldRejectAnotherSeller() {
        Consignment consignment = new Consignment();
        consignment.setId(1L);
        consignment.setSeller(user(100L));
        when(consignmentRepository.findById(1L)).thenReturn(Optional.of(consignment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> consignmentService.updateLogistics(1L, "SF123", 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You are not authorized to update this consignment", ex.getReason());
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
