package com.xsh.trueused.user.dto;

public record UserFollowDTO(
        Long userId,
        Long followerCount,
        Boolean following) {
}
