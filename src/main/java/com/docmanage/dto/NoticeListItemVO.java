package com.docmanage.dto;

import com.docmanage.entity.Notice;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeListItemVO {

    private Long id;
    private String title;
    private String content;
    private String publisherName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    public static NoticeListItemVO from(Notice notice) {
        return NoticeListItemVO.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .publisherName(notice.getPublisherName())
                .publishTime(notice.getPublishTime())
                .build();
    }
}
