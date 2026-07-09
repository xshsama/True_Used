package com.xsh.trueused.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.entity.Order;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.Review;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.order.enums.OrderStatus;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.review.dto.CreateReviewRequest;
import com.xsh.trueused.review.dto.ReplyReviewRequest;
import com.xsh.trueused.review.dto.ReviewDTO;
import com.xsh.trueused.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewShouldPersistCompletedBuyerOrderWithImages() {
        Order order = order(900L, 100L, 200L, OrderStatus.COMPLETED);
        CreateReviewRequest request = reviewRequest(900L, 5, "成色准确", false, List.of("a.jpg", "b.jpg"));
        when(orderRepository.findById(900L)).thenReturn(Optional.of(order));
        when(reviewRepository.findByOrderId(900L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(300L);
            return review;
        });

        ReviewDTO dto = reviewService.createReview(request, 100L);

        assertEquals(300L, dto.getId());
        assertEquals(900L, dto.getOrderId());
        assertEquals(5, dto.getRating());
        assertEquals(List.of("a.jpg", "b.jpg"), dto.getImages());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review saved = reviewCaptor.getValue();
        assertSame(order, saved.getOrder());
        assertSame(order.getProduct(), saved.getProduct());
        assertSame(order.getBuyer(), saved.getBuyer());
        assertSame(order.getSeller(), saved.getSeller());
        assertEquals(2, saved.getImages().size());
        assertSame(saved, saved.getImages().get(0).getReview());
    }

    @Test
    void createReviewShouldRejectAnotherBuyer() {
        when(orderRepository.findById(900L)).thenReturn(Optional.of(order(900L, 100L, 200L, OrderStatus.COMPLETED)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(reviewRequest(900L, 5, "好", false, List.of()), 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You can only review your own orders", ex.getReason());
    }

    @Test
    void createReviewShouldRejectUncompletedOrder() {
        when(orderRepository.findById(900L)).thenReturn(Optional.of(order(900L, 100L, 200L, OrderStatus.SHIPPED)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(reviewRequest(900L, 5, "好", false, List.of()), 100L));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Order must be completed to review", ex.getReason());
    }

    @Test
    void createReviewShouldRejectDuplicateOrderReview() {
        when(orderRepository.findById(900L)).thenReturn(Optional.of(order(900L, 100L, 200L, OrderStatus.COMPLETED)));
        when(reviewRepository.findByOrderId(900L)).thenReturn(Optional.of(new Review()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createReview(reviewRequest(900L, 5, "好", false, List.of()), 100L));

        assertEquals(409, ex.getStatusCode().value());
        assertEquals("Review already exists for this order", ex.getReason());
    }

    @Test
    void replyReviewShouldRejectWrongSeller() {
        Review review = review(300L, 100L, 200L);
        when(reviewRepository.findById(300L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.replyReview(300L, replyRequest("感谢"), 999L));

        assertEquals(403, ex.getStatusCode().value());
        assertEquals("You can only reply to reviews of your products", ex.getReason());
    }

    @Test
    void replyReviewShouldUpdateSellerReply() {
        Review review = review(300L, 100L, 200L);
        when(reviewRepository.findById(300L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        ReviewDTO dto = reviewService.replyReview(300L, replyRequest("感谢认可"), 200L);

        assertEquals("感谢认可", dto.getSellerReply());
        assertNotNull(review.getSellerReplyAt());
        verify(reviewRepository).save(review);
    }

    private static CreateReviewRequest reviewRequest(Long orderId, Integer rating, String content,
            Boolean isAnonymous, List<String> images) {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setOrderId(orderId);
        request.setRating(rating);
        request.setContent(content);
        request.setIsAnonymous(isAnonymous);
        request.setImages(images);
        return request;
    }

    private static ReplyReviewRequest replyRequest(String content) {
        ReplyReviewRequest request = new ReplyReviewRequest();
        request.setReplyContent(content);
        return request;
    }

    private static Review review(Long reviewId, Long buyerId, Long sellerId) {
        Review review = new Review();
        review.setId(reviewId);
        review.setOrder(order(900L, buyerId, sellerId, OrderStatus.COMPLETED));
        review.setProduct(review.getOrder().getProduct());
        review.setBuyer(review.getOrder().getBuyer());
        review.setSeller(review.getOrder().getSeller());
        review.setRating(5);
        review.setContent("好");
        return review;
    }

    private static Order order(Long orderId, Long buyerId, Long sellerId, OrderStatus status) {
        User buyer = user(buyerId, "buyer");
        User seller = user(sellerId, "seller");
        Product product = new Product();
        product.setId(10L);
        product.setTitle("MacBook Pro");
        product.setPrice(new BigDecimal("6800.00"));
        product.setSeller(seller);

        Order order = new Order();
        order.setId(orderId);
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setProduct(product);
        order.setPrice(product.getPrice());
        order.setStatus(status);
        return order;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
