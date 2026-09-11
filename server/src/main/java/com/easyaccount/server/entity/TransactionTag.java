package com.easyaccount.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "transaction_tags")
public class TransactionTag {
    @EmbeddedId
    private TransactionTagId id;

    @Embeddable
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TransactionTagId implements java.io.Serializable {
        @Column(name = "transaction_id")
        private Long transactionId;
        @Column(name = "tag_id")
        private Long tagId;
    }
}
