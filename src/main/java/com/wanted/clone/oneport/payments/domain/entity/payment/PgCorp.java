package com.wanted.clone.oneport.payments.domain.entity.payment;

import com.wanted.clone.oneport.payments.domain.exception.UnsupportedPgCorpException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PgCorp {
    TOSS(0),
    NHN_KCP(1);

    private int code;

    public static PgCorp valueOfCode(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid code: " + code));
    }

    public static PgCorp from(String name) {
        if (name == null || name.isBlank()) {
            throw UnsupportedPgCorpException.forName(name);
        }

        String normalizedName = name.trim()
                .replace('-', '_')
                .toUpperCase();

        return Arrays.stream(values())
                .filter(pgCorp -> pgCorp.name().equals(normalizedName))
                .findFirst()
                .orElseThrow(() -> UnsupportedPgCorpException.forName(name));
    }
}
