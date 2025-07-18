module ru.rebelsouth.demo1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.rebelsouth.demo1 to javafx.fxml;
    exports ru.rebelsouth.demo1.Analyzer;
    opens ru.rebelsouth.demo1.Analyzer to javafx.fxml;
}