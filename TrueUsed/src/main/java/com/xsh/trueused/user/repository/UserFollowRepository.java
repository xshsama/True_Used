package com.xsh.trueused.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.xsh.trueused.entity.UserFollow;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    boolean existsByFollower_IdAndFollowed_Id(Long followerId, Long followedId);

    long countByFollowed_Id(Long followedId);

    void deleteByFollower_IdAndFollowed_Id(Long followerId, Long followedId);

    @Query(
            value = "SELECT f FROM UserFollow f JOIN FETCH f.followed WHERE f.follower.id = :followerId",
            countQuery = "SELECT COUNT(f) FROM UserFollow f WHERE f.follower.id = :followerId")
    Page<UserFollow> findFollowingByFollowerId(Long followerId, Pageable pageable);
}
