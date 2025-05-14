module com.example.voyage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires javafx.web;
    requires javafx.swing;

    // Fix the module names to use automatic module names derived from JAR filenames
    requires stripe.java; // Instead of com.stripe.stripe.java
    requires org.json; // Required by stripe-java
    requires jbcrypt; // Instead of org.mindrot.jbcrypt

    // If you have gson dependency
    requires com.google.gson;
    requires jdk.jsobject;

    opens com.example.voyage to javafx.fxml;

    // Add opens directives for Gson serialization
    opens com.example.voyage.user.models to com.google.gson;
    opens com.example.voyage.voyage.models to com.google.gson;

    exports com.example.voyage;

    // Open all controller packages to javafx.fxml
    opens com.example.voyage.user.frontoffice.controllers to javafx.fxml;
    opens com.example.voyage.user.backoffice.controllers to javafx.fxml;
    opens com.example.voyage.voyage.frontoffice.controllers to javafx.fxml;
    opens com.example.voyage.voyage.backoffice.controllers to javafx.fxml;
    opens com.example.voyage.payment.controllers to javafx.fxml;

    // Export necessary packages
    exports com.example.voyage.user.models;
    exports com.example.voyage.user.services;
    exports com.example.voyage.voyage.models;
    exports com.example.voyage.voyage.services;
    exports com.example.voyage.payment.models;
    exports com.example.voyage.payment.services;
    exports com.example.voyage.utils;

    // Export controller packages
    exports com.example.voyage.user.frontoffice.controllers;
    exports com.example.voyage.user.backoffice.controllers;
    exports com.example.voyage.voyage.frontoffice.controllers;
    exports com.example.voyage.voyage.backoffice.controllers;
    exports com.example.voyage.payment.controllers;
}