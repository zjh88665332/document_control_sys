package com.docmanage.repository;

import com.docmanage.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Page<Friend> findByFriendIdOrderByApplyTimeDesc(Long friendId, Pageable pageable);

    Page<Friend> findByUserIdOrderByApplyTimeDesc(Long userId, Pageable pageable);

    @Query("SELECT f FROM Friend f WHERE f.isDel = 0 AND f.status = 1 " +
            "AND (f.userId = :userId OR f.friendId = :userId) " +
            "ORDER BY COALESCE(f.handleTime, f.applyTime) DESC")
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);

    Optional<Friend> findByIdAndFriendIdAndStatusAndIsDel(Long id, Long friendId, Integer status, Integer isDel);

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE f.isDel = 0 AND f.status = :status " +
            "AND ((f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId))")
    boolean existsFriendship(@Param("userId") Long userId,
                             @Param("friendId") Long friendId,
                             @Param("status") Integer status);

    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE f.status = 0 AND f.isDel = 0 " +
            "AND ((f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId))")
    boolean existsPendingApply(@Param("userId") Long userId, @Param("friendId") Long friendId);

    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);

    long countByFriendIdAndStatusAndIsDelAndIsRead(Long friendId, Integer status, Integer isDel, Integer isRead);

    @Modifying
    @Query("UPDATE Friend f SET f.isRead = 1 WHERE f.friendId = :userId AND f.status = 0 AND f.isDel = 0 AND f.isRead = 0")
    void markReceivedAppliesRead(@Param("userId") Long userId);
}
