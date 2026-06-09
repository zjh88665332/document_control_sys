package com.docmanage.dto;

import com.docmanage.entity.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeDetailVO {

    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;
    private Integer viewCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    public static NoticeDetailVO from(Notice notice) {
        return NoticeDetailVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .publisherId(notice.getPublisherId())
                .publisherName(notice.getPublisherName())
                .viewCount(notice.getViewCount())
                .publishTime(notice.getPublishTime())
                .build();
    }
}
