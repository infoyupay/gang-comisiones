Project-specific Development Guidelines

Build and Configuration
- Java and Toolchain
  - Uses Gradle Kotlin DSL with wrapper. Java toolchain is set to JDK 24 (build.gradle.kts: java.toolchain.languageVersion = 24). Ensure you have a compatible JDK 24 installed if you are not using the Gradle toolchain download.
  - JPMS is enabled (module-info.java). The application plugin is configured with main module com.yupay.gangcomisiones and main class com.yupay.gangcomisiones.HelloApplication (application block).
- JavaFX Packaging
  - JavaFX plugin org.openjfx.javafxplugin 0.1.0 and org.beryx.jlink 3.1.3 are used. JavaFX version 24 with modules javafx.controls and javafx.fxml.
  - jlink task produces a custom runtime image zip: build/distributions/app-${javafx.platform.classifier}.zip and launcher name app.
- Persistence
  - JPA API 3.2.0 with EclipseLink 4.0.7. PostgreSQL driver 42.7.7 is included. Persistence unit is configured under src/main/resources/META-INF/persistence.xml. Integration tests rely on a test properties file (see Testing section).
- Logging
  - SLF4J 2.0.17 with Logback 1.5.18. Log configuration is initialized in tests via LogConfig.initLogging().

Common Gradle Tasks
- Build all: ./gradlew clean build
- Run unit tests only (default test task runs all JUnit5 tests): ./gradlew test
- Create runtime image (requires JavaFX modules): ./gradlew jlink

Testing
- Framework
  - JUnit 5 (Jupiter). junit-bom 5.13.4 with junit-jupiter-api/params and junit-jupiter-engine are configured. Mockito 5.19.0 available for unit tests.
  - All tests execute on JUnit Platform via useJUnitPlatform().
- Test layout
  - Unit tests: src/test/java/... Simple examples include PasswordUtilTest and EntityUnitTests. These do not require a running database.
  - Integration tests (require local PostgreSQL): classes extending AbstractPostgreIntegrationTest will bootstrap AppContext with test JPA properties. Example packages: model/*IntegrationTest.java, services/UserServiceIntegrationTest.java, etc.
- Database requirements for integration tests
  - Local PostgreSQL must be running and reachable with the connection settings in src/test/resources/com/yupay/gangcomisiones/dummy-jpa.properties:
    - url: jdbc:postgresql://localhost:5432/gang_comision_test
    - user: postgres
    - password: (empty by default)
  - Adjust user/password or URL in the dummy properties if your local DB differs. Set appropriate permissions and create database gang_comision_test ahead of running tests.
  - EclipseLink provider is used; logging level defaults to FINE in the dummy properties.
- How tests bootstrap JPA
  - AbstractPostgreIntegrationTest@init calls LogConfig.initLogging and obtains AppContext via AppContext.getInstance(DummyHelpers.getDummyJpaProperties()). DummyHelpers resolves the properties from the test resources classpath.
  - All integration tests requiring test database connection shall extend AbstractPostgreIntegrationTest
- Running tests
  - Run all unit tests (faster, no DB needed):
    - Example verified now: ./gradlew test --tests "*PasswordUtilTest"
    - Or run a package/class: ./gradlew test --tests "com.yupay.gangcomisiones.model.EntityUnitTests"
  - Run one test method:
    - ./gradlew test --tests "com.yupay.gangcomisiones.security.PasswordUtilTest.testVerifyPasswordSuccess"
  - Run all tests including integration tests (requires DB configured as above): ./gradlew test
    - If integration tests fail due to DB, you can temporarily exclude them by pattern, e.g.: ./gradlew test -Dtest.single="*UnitTests"
- Adding a new test
  - Place new unit tests under src/test/java with package com.yupay.gangcomisiones or relevant subpackages.
  - Example minimal test (used to validate this guide):
    - File: src/test/java/com/yupay/gangcomisiones/SampleGuidelinesDemoTest.java
      - Contents:
        - package com.yupay.gangcomisiones;
        - import org.junit.jupiter.api.Test;
        - import static org.junit.jupiter.api.Assertions.assertEquals;
        - class SampleGuidelinesDemoTest { @Test void additionWorks() { assertEquals(4, 2 + 2); } }
    - Run it only:
      - ./gradlew test --tests "com.yupay.gangcomisiones.SampleGuidelinesDemoTest"
    - This test was executed successfully during preparation of this guide. The file has now been removed to keep the repository clean; follow the snippet above to recreate when needed.

Additional Development Information
- Module system
  - The project uses module-info.java. When adding new packages or using reflective frameworks, ensure proper exports/opens in module-info to satisfy JPA, JavaFX FXML, and testing access where necessary.
- Entities and equals/hashCode
  - Entities (Bank, Concept, GlobalConfig, Transaction, ReversalRequest, User) have equals/hashCode semantics covered by EntityUnitTests. Maintain consistency if you add fields; update tests if identity semantics change.
- Security utils
  - PasswordUtil performs salt generation and password hashing/verification. It intentionally has a private constructor that throws on reflective instantiation; tests assert this. Reuse PasswordUtil.verifyPassword for authentication logic.
- Logging and uncaught exceptions
  - UncaughtExceptionLogger integrates with thread default uncaught handler; see UncaughtExceptionLoggerTest for expected behavior and how logging is asserted. Maintain SLF4J API usage across modules.
- AppContext and lifecycle in tests
  - AppContext.getInstance(Path to properties) initializes JPA layer and resources. Integration tests should close with AppContext.shutdown() in @AfterAll (handled in AbstractPostgreIntegrationTest).
- Database schema artifacts
  - model/gangcommission.sql and model/gangcommission.dbm capture schema. Keep these synchronized with JPA changes. The /model/MODEL-CONVENTIONS.md contains modeling conventions for this project.
- Architecture
  - All persistence services are interfaced in com.yupay.gangcomisiones.services, then implemented in com.yupay.gangcomisiones.services.impl
  - All persistence services uses CompletableFuture and are asynchronous.
  - When creating a new persistence service, it shall be exposed in AppContext.
  - All persistence services receive when instanciating in the AppContext constructor, a reference to the entity manager factory and jdbcExecutor.
- Code style
  - Java 24 features are allowed and prefered. Keep UTF-8 encoding and Javadoc headers, annotations from org.jetbrains are used for contracts and nullability. Prefer SLF4J for logging.
  - Don't use experimental or preview features, only standard features of the current JDK version.
  - New entities, DTO and view model entities shall use Lombok, with NoArgsConstructor, AllArgsConstructor, Getter, Setter and Builder.
  - Creation of objects related to entities annotated with lombok shall be made with the Lombok Builder.
  - All packages containing classes must have a package-info.java with javadoc comment and must include  @author InfoYupay SACS and @version 1.0
  - All Classes must have a class javadoc comment and must include @param if applicable and also  @author InfoYupay SACS and @version 1.0
  - All inner and static classes also must have the class javadoc as stated for classes.
  - All methods must have a javadoc comment including all relevant @param, @return and @throws when applicable.
  - All record classes must use @param javadoc to document the record fields.

Troubleshooting
- Gradle test discovery issues
  - Ensure tests are named with *Test suffix and are public or package-private classes with @Test methods. The Gradle launcher and Jupiter engine are included; use the --tests filter to avoid scanning large suites unnecessarily.
- Database connection failures in ITs
  - Confirm local PostgreSQL is up, database exists, and credentials match dummy-jpa.properties. Adjust the properties file rather than production persistence.xml for test-only changes.
- JavaFX packaging errors
  - Ensure you run jlink on a platform with matching JavaFX platform classifier. The default build on headless CI may not include JavaFX modules; skip jlink there.
