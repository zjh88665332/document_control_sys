package com.docmanage.dto;

import com.docmanage.entity.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminNoticeListItemVO {

    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private Integer isTop;
    private Integer viewCount;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    public static AdminNoticeListItemVO from(Notice notice) {
        return AdminNoticeListItemVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .publisherId(notice.getPublisherId())
                .publisherName(notice.getPublisherName())
                .isTop(notice.getIsTop())
                .viewCount(notice.getViewCount())
                .status(notice.getStatus())
                .publishTime(notice.getPublishTime())
                .build();
    }
}
