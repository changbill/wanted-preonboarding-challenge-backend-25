package com.wanted.clone.oneport;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.wanted.clone.oneport.payments.application.port.out.pg.PaymentAPIs;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchTests {
    @Test
    public void controller_mustHaveAboutControllerAnnotation_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().resideInAPackage("..presentation.web")
                .should().beAnnotatedWith(RestController.class)
                .orShould().beAnnotatedWith(Controller.class)
                .orShould().haveSimpleNameContaining("Controller");
        rule.check(classes);
    }

    @Test
    public void controller_mustContainControllerInNaming_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().resideInAPackage("..presentation.web")
                .should().haveSimpleNameContaining("Controller");

        rule.check(classes);
    }

    @Test
    public void controller_dependencies_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().haveNameMatching(".*Repository")
                .should().onlyHaveDependentClassesThat().haveNameMatching(".*Service")
                .orShould().onlyHaveDependentClassesThat().resideInAnyPackage("..application.service..", "..infrastructure.persistence..");

        rule.check(classes);
    }

    @Test
    public void interface_mustHaveInterfaceAnnotation_thenTrue() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().resideInAPackage("..application.port.in")
                .should().beInterfaces()
                .andShould().onlyBeAccessed().byClassesThat().resideInAnyPackage("..presentation.web", "..application.service");
        rule.check(classes);
    }

    @Test
    public void service_dependency_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().resideInAPackage("..application.service")
                .should().dependOnClassesThat().resideInAnyPackage("..port.out..");

        rule.check(classes);
    }

    @Test
    public void paymentAPIs_NamingConvention_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = classes().that().implement(PaymentAPIs.class)
                .should().haveSimpleNameEndingWith("Payment");
        rule.check(classes);
    }

    @Test
    public void application_layer_mustNotDependOnPresentationOrInfrastructure_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = noClasses().that().resideInAPackage("..payments.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..payments.presentation..", "..payments.infrastructure..");

        rule.check(classes);
    }

    @Test
    public void core_domain_layer_mustNotDependOnApplicationPresentationOrInfrastructure_rule() {
        JavaClasses classes = importedMainClasses();
        ArchRule rule = noClasses().that().resideInAPackage("..payments.domain..")
                .and().resideOutsideOfPackage("..payments.domain.entity.settlements..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..payments.application..", "..payments.presentation..", "..payments.infrastructure..");

        rule.check(classes);
    }

    private JavaClasses importedMainClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.wanted.clone.oneport");
    }
}
