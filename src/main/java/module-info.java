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

    /*==================*
     * Open directives. *
     *==================*/
    //Reflective acces to JavaFx.
    opens com.yupay.gangcomisiones to javafx.fxml;
}