module ru.gb.javafxmessenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ru.gb.javafxmessenger.client;
    opens ru.gb.javafxmessenger.client to javafx.fxml;
}