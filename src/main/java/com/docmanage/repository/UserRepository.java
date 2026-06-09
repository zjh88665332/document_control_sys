package com.docmanage.repository;

import com.docmanage.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    long countByRole(String role);

    long countByRoleAndStatus(String role, Integer status);

    @Query("SELECT u FROM User u WHERE u.id <> :userId AND u.role = 'user' AND u.status = 1 " +
            "AND (u.realName LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE u.role = :role " +
            "AND (:username IS NULL OR u.username LIKE CONCAT('%', :username, '%')) " +
            "AND (:realName IS NULL OR u.realName LIKE CONCAT('%', :realName, '%')) " +
            "AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%')) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "ORDER BY u.createTime DESC")
    Page<User> findByRoleAndFilters(@Param("role") String role,
                                    @Param("username") String username,
                                    @Param("realName") String realName,
                                    @Param("phone") String phone,
                                    @Param("status") Integer status,
                                    Pageable pageable);
}
