package com.example.cryptotrading.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericPage<T> {

    private final List<T> data;
    private final long totalCount;
    private final int pageNumber;
    private final int pageSize;
}
