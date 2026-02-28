package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.OrderSideEntity.TABLE_NAME;

import com.example.cryptotrading.domain.OrderSideCodeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = TABLE_NAME)
@Getter
@Setter
@NoArgsConstructor
public class OrderSideEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_order_side";

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 4)
    private OrderSideCodeEnum code;

    @Column(nullable = false, length = 50)
    private String description;

    public OrderSideEntity(Long id, OrderSideCodeEnum code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }
}
