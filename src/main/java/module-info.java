module ca.acoii.nationbuilder.nationbuilder {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens ca.acoii.nationbuilder.nationbuilder to javafx.fxml;
    exports ca.acoii.nationbuilder.nationbuilder;
}