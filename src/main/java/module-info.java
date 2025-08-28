/**
 * Module declaration for the monolythic com.yupay.gangcomisiones application.
 * <br/>
 * This module specifies the required dependencies and package
 * accessibility for the application.
 *
 * @author InfoYupay SACS
 */
module com.yupay.gangcomisiones {
    /*=======================*
     * Java FX dependencies. *
     *=======================*/
    requires javafx.controls;
    requires javafx.fxml;
    requires net.synedra.validatorfx;

    /*==================================*
     * Persistence - JDBC dependencies. *
     *==================================*/
    requires jakarta.persistence;
    requires java.sql;
    requires org.postgresql.jdbc;

    /*====================*
     * Logging libraries. *
     *====================*/
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;

    /*======================*
     * Code analysis tools. *
     *======================*/
    requires org.jetbrains.annotations;

    /*========================================*
     * Boilerplate and code generation tools. *
     *========================================*/
    requires static lombok;

    /*==================*
     * Open directives. *
     *==================*/
    //Reflective acces to JavaFx.
    opens com.yupay.gangcomisiones to javafx.fxml;
    //Reflective access to JPA
    opens com.yupay.gangcomisiones.model to jakarta.persistence, eclipselink;
}