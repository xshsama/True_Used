package com.xsh.trueused.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.xsh.trueused.entity.User;
import com.xsh.trueused.entity.UserFollow;
import com.xsh.trueused.enums.ProductStatus;
import com.xsh.trueused.order.enums.OrderStatus;
import com.xsh.trueused.order.repository.OrderRepository;
import com.xsh.trueused.product.repository.ProductRepository;
import com.xsh.trueused.user.dto.FollowingUserDTO;
import com.xsh.trueused.user.dto.UserFollowDTO;
import com.xsh.trueused.user.repository.UserFollowRepository;
import com.xsh.trueused.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public UserFollowDTO follow(Long followerId, Long followedId) {
        ensureNotSelf(followerId, followedId);

        User follower = requireUser(followerId);
        User followed = requireUser(followedId);

        if (!userFollowRepository.existsByFollower_IdAndFollowed_Id(followerId, followedId)) {
            UserFollow follow = new UserFollow();
            follow.setFollower(follower);
            follow.setFollowed(followed);
            userFollowRepository.save(follow);
        }

        return status(followerId, followedId);
    }

    @Transactional
    public UserFollowDTO unfollow(Long followerId, Long followedId) {
        ensureNotSelf(followerId, followedId);
        requireUser(followedId);
        userFollowRepository.deleteByFollower_IdAndFollowed_Id(followerId, followedId);
        return status(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public long followerCount(Long followedId) {
        return userFollowRepository.countByFollowed_Id(followedId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        if (followerId == null || followerId.equals(followedId)) {
            return false;
        }
        return userFollowRepository.existsByFollower_IdAndFollowed_Id(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public UserFollowDTO status(Long followerId, Long followedId) {
        return new UserFollowDTO(
                followedId,
                followerCount(followedId),
                isFollowing(followerId, followedId));
    }

    @Transactional(readOnly = true)
    public Page<FollowingUserDTO> listFollowing(Long followerId, Pageable pageable) {
        return userFollowRepository.findFollowingByFollowerId(followerId, pageable)
                .map(this::toFollowingUserDTO);
    }

    private FollowingUserDTO toFollowingUserDTO(UserFollow follow) {
        User user = follow.getFollowed();
        long sellingCount = productRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("seller").get("id"), user.getId()),
                cb.equal(root.get("status"), ProductStatus.ON_SALE),
                cb.equal(root.get("isDeleted"), false)));
        long soldCount = orderRepository.count((root, query, cb) -> cb.and(
                cb.equal(root.get("seller").get("id"), user.getId()),
                cb.equal(root.get("status"), OrderStatus.COMPLETED)));

        return new FollowingUserDTO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getCoverImage(),
                user.getLocation(),
                user.getCreatedAt(),
                follow.getCreatedAt(),
                (int) sellingCount,
                (int) soldCount,
                followerCount(user.getId()));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private void ensureNotSelf(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        }
    }
}
