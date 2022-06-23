module ru.gb.javafxmessenger {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.gb.javafxmessenger.client;
    opens ru.gb.javafxmessenger.client to javafx.fxml;
}