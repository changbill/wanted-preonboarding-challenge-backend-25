package com.wanted.clone.oneport.payments.domain.entity;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class DomainPersistenceDependencyTests {
    @Test
    void core_payment_domain_models_do_not_depend_on_jpa() {
        JavaClasses classes = new ClassFileImporter().importPackages(
                "com.wanted.clone.oneport.payments.domain.entity.order",
                "com.wanted.clone.oneport.payments.domain.entity.payment"
        );

        ArchRule rule = noClasses()
                .that().haveNameMatching(".*\\.(Order|OrderItem|PurchaseOrderId|PaymentLedger|CardPayment|TransactionType)")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..");

        rule.check(classes);
    }
}
