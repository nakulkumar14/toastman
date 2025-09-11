module com.toast.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires org.fxmisc.richtext;
    requires java.logging;
    requires org.slf4j;

    opens com.toast.demo to javafx.fxml;
    exports com.toast.demo;
    exports com.toast.demo.model;
}