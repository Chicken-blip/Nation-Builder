module ca.acoii.nationbuilder.nationbuilder {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;


    opens ca.acoii.nationbuilder.nationbuilder to javafx.fxml, com.fasterxml.jackson.databind;
    exports ca.acoii.nationbuilder.nationbuilder;
}