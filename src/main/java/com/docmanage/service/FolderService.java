package com.docmanage.service;

import com.docmanage.common.BusinessException;
import com.docmanage.dto.CreateFolderRequest;
import com.docmanage.dto.FolderVO;
import com.docmanage.entity.Folder;
import com.docmanage.entity.User;
import com.docmanage.repository.DocFileRepository;
import com.docmanage.repository.FolderRepository;
import com.docmanage.repository.UserRepository;
import com.docmanage.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final DocFileRepository docFileRepository;
    private final UserRepository userRepository;
    private final OperationLogService operationLogService;

    @Transactional(readOnly = true)
    public List<FolderVO> getFolderTree() {
        User user = findCurrentUser();
        List<Folder> folders = folderRepository.findByUserIdOrderByCreateTimeAsc(user.getId());
        Map<Long, List<Folder>> childrenMap = folders.stream()
                .collect(Collectors.groupingBy(Folder::getParentId));

        FolderVO root = FolderVO.builder().id(0L).name("全部文件").parentId(-1L).children(new ArrayList<>()).build();
        root.getChildren().addAll(buildChildren(0L, childrenMap));
        return List.of(root);
    }

    @Transactional
    public FolderVO createFolder(CreateFolderRequest request) {
        User user = findCurrentUser();
        Long parentId = request.getParentId() != null ? request.getParentId() : 0L;
        if (parentId > 0) {
            folderRepository.findByIdAndUserId(parentId, user.getId())
                    .orElseThrow(() -> new BusinessException("父文件夹不存在"));
        }

        Folder folder = new Folder();
        folder.setUserId(user.getId());
        folder.setName(request.getName().trim());
        folder.setParentId(parentId);
        Folder saved = folderRepository.save(folder);

        operationLogService.log("文件夹", "创建", saved.getId(), saved.getName(), "创建文件夹");
        return FolderVO.from(saved);
    }

    @Transactional
    public void deleteFolder(Long id) {
        User user = findCurrentUser();
        Folder folder = folderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException(404, "文件夹不存在"));

        if (folderRepository.countByParentIdAndUserId(id, user.getId()) > 0) {
            throw new BusinessException("请先删除子文件夹");
        }
        if (docFileRepository.countByFolderIdAndIsDeleted(id, 0) > 0) {
            throw new BusinessException("文件夹内还有文件，无法删除");
        }

        folderRepository.delete(folder);
        operationLogService.log("文件夹", "删除", id, folder.getName(), "删除文件夹");
    }

    public void validateFolderOwnership(Long folderId, Long userId) {
        if (folderId == null || folderId == 0) {
            return;
        }
        folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new BusinessException("文件夹不存在"));
    }

    private List<FolderVO> buildChildren(Long parentId, Map<Long, List<Folder>> childrenMap) {
        List<Folder> children = childrenMap.getOrDefault(parentId, List.of());
        List<FolderVO> result = new ArrayList<>();
        for (Folder folder : children) {
            FolderVO vo = FolderVO.from(folder);
            vo.setChildren(buildChildren(folder.getId(), childrenMap));
            result.add(vo);
        }
        return result;
    }

    private User findCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未授权"));
    }
}
