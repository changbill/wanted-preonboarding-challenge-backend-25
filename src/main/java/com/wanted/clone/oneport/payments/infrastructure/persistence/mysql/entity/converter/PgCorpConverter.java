package com.wanted.clone.oneport.payments.infrastructure.persistence.mysql.entity.converter;

import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PgCorpConverter implements AttributeConverter<PgCorp, Integer> {
    @Override
    public Integer convertToDatabaseColumn(PgCorp pgCorp) {
        if (pgCorp == null) {
            return null;
        }
        return pgCorp.getCode();
    }

    @Override
    public PgCorp convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        return PgCorp.valueOfCode(code);
    }
}
