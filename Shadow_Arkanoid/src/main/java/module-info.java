module se233.shadow_arkanoid {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.shadow_arkanoid to javafx.fxml;
    exports se233.shadow_arkanoid;
}