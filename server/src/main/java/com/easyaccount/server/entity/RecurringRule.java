package com.easyaccount.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "recurring_rules")
public class RecurringRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category_id")
    private Long categoryId;
    private Double amount;
    private Integer type;
    private String note;
    @Column(name = "period_type")
    private Integer periodType;
    @Column(name = "next_date_ms")
    private Long nextDateMs;
    private Boolean enabled;
}
