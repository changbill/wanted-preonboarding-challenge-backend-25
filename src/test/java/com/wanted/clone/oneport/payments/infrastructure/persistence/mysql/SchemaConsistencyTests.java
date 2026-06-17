package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

class SchemaConsistencyTests {

    @Test
    void createSchema_MatchesJpaMappingNamesAndKeys() throws IOException {
        String schema = new String(
                Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("initdb/create_schema.sql"))
                        .readAllBytes(),
                StandardCharsets.UTF_8
        ).replaceAll("\\s+", " ");

        Assertions.assertTrue(schema.contains("`order_id` VARCHAR(255) NOT NULL COMMENT '전체 주문번호 - FK'"));
        Assertions.assertTrue(schema.contains("PRIMARY KEY (order_id, item_idx)"));
        Assertions.assertTrue(schema.contains("CREATE TABLE `card_payment`"));
        Assertions.assertTrue(schema.contains("`payment_key` VARCHAR(255) NOT NULL COMMENT '결제번호'"));
        Assertions.assertFalse(schema.contains("CREATE TABLE `card_payment_ledger`"));
    }
}
