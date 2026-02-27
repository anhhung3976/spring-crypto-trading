package com.example.cryptotrading.entity;

import static com.example.cryptotrading.entity.CurrencyEntity.TABLE_NAME;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class CurrencyEntity extends BaseEntity {

    public static final String TABLE_NAME = "crypto_currency";

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    public CurrencyEntity(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
}
