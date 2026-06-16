package com.docmanage.repository;

import com.docmanage.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUserIdOrderByCreateTimeAsc(Long userId);

    Optional<Folder> findByIdAndUserId(Long id, Long userId);

    long countByParentIdAndUserId(Long parentId, Long userId);
}
