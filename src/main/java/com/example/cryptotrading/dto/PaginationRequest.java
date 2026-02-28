package com.example.cryptotrading.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class PaginationRequest {

    private Integer pageNumber;
    private Integer pageSize;
    private String sortOrder;
    private String sortBy;

    public Pageable toPageableOrDefault() {
        if (pageNumber != null && pageSize != null) {
            return Pageable.ofSize(pageSize).withPage(pageNumber);
        }
        return Pageable.ofSize(10000).withPage(0);
    }
}
