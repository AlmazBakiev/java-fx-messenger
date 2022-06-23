module ru.gb.javafxmessenger {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.gb.javafxmessenger to javafx.fxml;
    exports ru.gb.javafxmessenger;
}