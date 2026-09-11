package com.easyaccount.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    private Integer type;
    @Column(name = "category_id")
    private Long categoryId;
    private String note;
    @Column(name = "date_ms")
    private Long dateMs;
    @Column(name = "created_at")
    private Long createdAt;
}
