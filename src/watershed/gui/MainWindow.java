package watershed.gui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class MainWindow extends Application {
    public MainWindow() {
    }
    @Override
    public void init() {
    }

    @Override
    public void start(Stage primaryStage) {
        final Button button = new Button();
        button.setText("Hello Watershed");
        button.setOnAction((ActionEvent event) -> {
            System.out.println("Hello Watershed!");
        });
        final StackPane root = new StackPane();
        root.getChildren().add(button);
        final Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("Hello Watershed!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    @Override
    public void stop() {
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }
}