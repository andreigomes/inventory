package com.enterprise.inventory.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests to enforce Clean Architecture principles.
 * Ensures proper dependency directions and layer isolation.
 */
class CleanArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
        .importPackages("com.enterprise.inventory");

    @Test
    void should_follow_clean_architecture_layers() {
        ArchRule rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Presentation").definedBy("..presentation..")

            .whereLayer("Domain").mayNotAccessAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Application", "Domain")
            .whereLayer("Presentation").mayOnlyAccessLayers("Application", "Domain");

        rule.check(classes);
    }

    @Test
    void domain_should_not_depend_on_any_framework() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "com.fasterxml.jackson..",
                "org.hibernate.."
            );

        rule.check(classes);
    }

    @Test
    void entities_should_be_in_domain_model_package() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Entity")
            .and().doNotHaveSimpleName("InventoryEntity") // Infrastructure entity
            .should().resideInAPackage("..domain.model..");

        rule.check(classes);
    }

    @Test
    void repositories_should_be_interfaces_in_domain() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .and().areNotAssignableFrom(org.springframework.data.jpa.repository.JpaRepository.class)
            .should().beInterfaces()
            .andShould().resideInAPackage("..domain.repository..");

        rule.check(classes);
    }

    @Test
    void use_cases_should_be_in_application_layer() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage("..application.usecase..")
            .andShould().beAnnotatedWith("org.springframework.stereotype.Service");

        rule.check(classes);
    }

    @Test
    void controllers_should_only_access_application_layer() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..application..",
                "..presentation.dto..",
                "..shared..",
                "java..",
                "org.springframework..",
                "jakarta.validation..",
                "io.swagger..",
                "io.opentelemetry.."
            );

        rule.check(classes);
    }

    @Test
    void infrastructure_adapters_should_implement_domain_interfaces() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Adapter")
            .should().implement(
                classes().that().resideInAPackage("..domain.repository..")
            );

        rule.check(classes);
    }

    @Test
    void value_objects_should_be_immutable() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..shared.common..")
            .should().beFinal()
            .andShould().bePrivate();

        rule.check(classes);
    }

    @Test
    void domain_events_should_extend_base_event() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Event")
            .and().resideInAPackage("..domain.events..")
            .should().beAssignableTo("com.enterprise.shared.domain.events.DomainEvent");

        rule.check(classes);
    }
}
