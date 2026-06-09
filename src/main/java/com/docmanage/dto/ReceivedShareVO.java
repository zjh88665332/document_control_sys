package com.docmanage.dto;

import com.docmanage.entity.DocFile;
import com.docmanage.entity.ShareRecord;
import com.docmanage.entity.User;
import com.docmanage.util.FileTypeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceivedShareVO {

    private Long id;
    private String fileName;
    private String fileFormat;
    private String fileType;
    private String sharerName;
    private String sharerPhone;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shareTime;

    private Integer status;

    public static ReceivedShareVO from(ShareRecord share, DocFile file, User sharer) {
        String category = FileTypeUtils.resolveCategory(file.getFormat());
        return ReceivedShareVO.builder()
                .id(share.getId())
                .fileName(file.getName())
                .fileFormat(file.getFormat())
                .fileType(FileTypeUtils.resolveDisplayType(category))
                .sharerName(sharer.getRealName())
                .sharerPhone(sharer.getPhone())
                .remark(share.getRemark())
                .shareTime(share.getShareTime())
                .status(share.getStatus())
                .build();
    }
}
