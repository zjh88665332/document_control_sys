package com.docmanage.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
public class PageResult<T> {

    private long total;
    private List<T> list;

    public static <E, T> PageResult<T> from(Page<E> page, Function<E, T> mapper) {
        return PageResult.<T>builder()
                .total(page.getTotalElements())
                .list(page.getContent().stream().map(mapper).collect(Collectors.toList()))
                .build();
    }
}
