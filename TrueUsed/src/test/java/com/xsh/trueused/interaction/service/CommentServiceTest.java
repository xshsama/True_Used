package com.xsh.trueused.interaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.entity.Comment;
import com.xsh.trueused.entity.Product;
import com.xsh.trueused.entity.User;
import com.xsh.trueused.interaction.dto.CommentDTO;
import com.xsh.trueused.interaction.dto.CreateCommentRequest;
import com.xsh.trueused.interaction.repository.CommentRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createCommentShouldPersistProductReplyWithParent() {
        User user = user(100L, "buyer");
        Product product = product(10L);
        Comment parent = comment(30L, user, product, "父评论", false);
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(commentRepository.findById(30L)).thenReturn(Optional.of(parent));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(31L);
            return comment;
        });

        CommentDTO dto = commentService.createComment(new CreateCommentRequest(10L, null, "收到", 30L), 100L);

        assertEquals(31L, dto.id());
        assertEquals(10L, dto.productId());
        assertEquals("收到", dto.content());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comment saved = commentCaptor.getValue();
        assertSame(user, saved.getUser());
        assertSame(product, saved.getProduct());
        assertSame(parent, saved.getParent());
    }

    @Test
    void createCommentShouldRejectRequestWithoutProductOrTargetUser() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user(100L, "buyer")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> commentService.createComment(new CreateCommentRequest(null, null, "空目标", null), 100L));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("Either productId or targetUserId must be provided", ex.getReason());
    }

    @Test
    void getProductCommentsShouldHideDeletedReplies() {
        User author = user(100L, "buyer");
        Product product = product(10L);
        Comment parent = comment(1L, author, product, "主评论", false);
        parent.getReplies().add(comment(2L, user(101L, "seller"), product, "可见回复", false));
        parent.getReplies().add(comment(3L, user(102L, "muted"), product, "已删回复", true));

        when(commentRepository.findByProductId(10L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(java.util.List.of(parent)));

        Page<CommentDTO> result = commentService.getProductComments(10L, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        CommentDTO dto = result.getContent().get(0);
        assertEquals("主评论", dto.content());
        assertEquals(1, dto.replies().size());
        assertEquals("可见回复", dto.replies().get(0).content());
    }

    private static Comment comment(Long id, User user, Product product, String content, boolean deleted) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUser(user);
        comment.setProduct(product);
        comment.setContent(content);
        comment.setIsDeleted(deleted);
        return comment;
    }

    private static User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private static Product product(Long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }
}
