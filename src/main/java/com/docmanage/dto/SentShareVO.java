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
public class SentShareVO {

    private Long id;
    private String fileName;
    private String fileFormat;
    private String fileType;
    private String receiverName;
    private String receiverPhone;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shareTime;

    private Integer status;

    public static SentShareVO from(ShareRecord share, DocFile file, User receiver) {
        String category = FileTypeUtils.resolveCategory(file.getFormat());
        return SentShareVO.builder()
                .id(share.getId())
                .fileName(file.getName())
                .fileFormat(file.getFormat())
                .fileType(FileTypeUtils.resolveDisplayType(category))
                .receiverName(receiver.getRealName())
                .receiverPhone(receiver.getPhone())
                .remark(share.getRemark())
                .shareTime(share.getShareTime())
                .status(share.getStatus())
                .build();
    }
}
