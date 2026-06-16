package com.docmanage.dto;

import com.docmanage.entity.Folder;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class FolderVO {

    private Long id;
    private String name;
    private Long parentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Builder.Default
    private List<FolderVO> children = new ArrayList<>();

    public static FolderVO from(Folder folder) {
        return FolderVO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParentId())
                .createTime(folder.getCreateTime())
                .build();
    }
}
