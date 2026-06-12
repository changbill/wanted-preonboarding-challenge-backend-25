package com.wanted.clone.oneport;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchTests {
    @Test
    public void controller_mustHaveAboutControllerAnnotation_rule() {
        // ArchUnit 동작 원리는 검증 대상 Classes 패키지 단위로 ArchUnit 내부에 Import 후에 지정한 검증을 수행 한다.
        // 따라서 우리가 검증 하려는 소스 코드(클래스)가 포함된 Package를 Import 해야 한다.
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        // 검증 내용 작성
        ArchRule rule = classes().that().resideInAPackage("..presentation.web")
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(Controller.class)
                .orShould().haveSimpleNameContaining("Controller");
        // 작성된 내용 검증 실행
        rule.check(classes);
    }

    @Test
    public void controller_mustContainControllerInNaming_rule() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        ArchRule rule = classes().that().resideInAPackage("..presentation.web")
                .should().haveSimpleNameContaining("Controller");

        rule.check(classes);
    }

    @Test
    public void controller_dependencies_rule(){
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        ArchRule rule = classes().that().haveNameMatching(".*Repository")
                .should().onlyHaveDependentClassesThat().haveNameMatching(".*Service")
                .orShould().onlyHaveDependentClassesThat().resideInAnyPackage("..application.service..", "..infrastructure.persistence..");

        rule.check(classes);
    }



    @Test
    public void interface_mustHaveInterfaceAnnotation_thenTrue() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        ArchRule rule = classes().that().resideInAPackage("..application.port.in")
                .should().beInterfaces()
                .andShould().onlyBeAccessed().byClassesThat().resideInAnyPackage("..presentation.web", "..application.service");
        rule.check(classes);
    }

    @Test
    public void service_dependency_rule() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        ArchRule rule = classes().that().resideInAPackage("..application.service")
                .should().dependOnClassesThat().resideInAnyPackage("..port.out..");

        rule.check(classes);
    }

    @Test
    public void paymentAPIs_NamingConvention_rule() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.wanted.clone.oneport");
        ArchRule rule = classes().that().implement(PaymentAPIs.class)
                .should().haveSimpleNameEndingWith("Payment");
        rule.check(classes);
    }

}
