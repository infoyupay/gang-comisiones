import java.util.Locale

plugins {
    //Base java plugin
    java
    //Runnable apps plugin.
    application
    //JPMS (module-info) support.
    id("org.javamodularity.moduleplugin") version "1.8.15"
    //Improve javafx support.
    id("org.openjfx.javafxplugin") version "0.1.0"
    //Generate native images with jlink.
    id("org.beryx.jlink") version "3.1.3"
}

group = "com.yupay"
version = "1.0-SNAPSHOT"

//Dependencies repos.
repositories {
    mavenCentral()
}
//Version variables
val junitVersion = "5.12.1"

java {
    toolchain {
        //Using JDK 24.
        languageVersion = JavaLanguageVersion.of(24)
    }
}

//Compilation settings.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//Modular app settings.
application {
    mainModule.set("com.yupay.gangcomisiones")
    mainClass.set("com.yupay.gangcomisiones.HelloApplication")
}

//Configuration of JavaFx.
javafx {
    version = "24"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    //Forms validation
    implementation("net.synedra:validatorfx:0.6.1") {
        exclude(group = "org.openjfx")//avoid javafx conflicts.
    }

    //JUnit 5 for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")

    //Jakarta JPA and EclipseLink
    // https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    // https://mvnrepository.com/artifact/org.eclipse.persistence/eclipselink
    implementation("org.eclipse.persistence:eclipselink:4.0.7")

    //PostgreSQL driver.
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    implementation("org.postgresql:postgresql:42.7.7")

    //Logging with SLF4J and Logback.
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    runtimeOnly("ch.qos.logback:logback-classic:1.5.18")

    //Jetbrains annotations (@NotNull, @Contract)
    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:26.0.2")

    //Lombox to reduce boiler plate.
    // https://mvnrepository.com/artifact/org.projectlombok/lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    //Mockito for testing.
    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.19.0")
}

//Configuring junit platform.
tasks.withType<Test> {
    useJUnitPlatform()
}

//JLink configuration

jlink {
    imageZip.set(layout.buildDirectory.file("/distributions/app-${javafx.platform.classifier}.zip"))
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = "app" //runnable name.
    }
}
