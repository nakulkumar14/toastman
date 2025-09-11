module com.toast.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires org.fxmisc.richtext;
    requires java.logging;
    requires org.slf4j;
    requires com.google.gson;

    opens com.toast.demo to javafx.fxml;
    opens com.toast.demo.model to com.fasterxml.jackson.databind; // Add this line

    exports com.toast.demo;
    exports com.toast.demo.model;
}