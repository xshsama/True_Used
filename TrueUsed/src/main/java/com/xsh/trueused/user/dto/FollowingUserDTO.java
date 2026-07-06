package com.xsh.trueused.user.dto;

import java.time.Instant;

public record FollowingUserDTO(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String bio,
        String coverImage,
        String location,
        Instant createdAt,
        Instant followedAt,
        Integer sellingCount,
        Integer soldCount,
        Long followerCount) {
}
