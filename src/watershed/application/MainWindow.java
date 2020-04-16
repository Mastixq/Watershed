package watershed.application;

import javafx.stage.FileChooser;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;

import watershed.algorithm.*;

import java.io.File;
import java.io.IOException;

public class MainWindow extends Application {

    @Override
    public void init() {
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Watershed application");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("resources"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        String name = selectedFile.getName();

        int choice = 0;
        switch (choice) {
            case 0:
                new GradientWatershed("resources/"+name);
                break;
            case 1:
                new TopographicWatershed("resources/"+name);
                break;
        }
    }
    @Override
    public void stop() {
    }
    public static void main(String[] args) throws IOException {
//        launch(args);
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
//        new GradientWatershed("resources/"+"small.jpg");
        new TopographicWatershed("resources/"+"small.jpg");
        System.exit(0);
    }

}