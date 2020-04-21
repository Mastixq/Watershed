package watershed.application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import watershed.algorithm.DistanceWatershed;
import watershed.algorithm.GradientWatershed;
import java.io.File;
import java.io.IOException;
import java.util.function.UnaryOperator;

import static javafx.application.Platform.exit;


public class MainWindow extends Application {

    private static String currentFile;

    @Override
    public void init() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Watershed application");

        GridPane gridPane = createMainGridPane(primaryStage);
        Button gradientButton = new Button("Gradient");
        Button distanceButton = new Button("Distance");

        gradientButton.setPrefSize(180, 50);
        distanceButton.setPrefSize(180, 50);

        gridPane.add(gradientButton, 1, 2, 2, 1);
        gridPane.add(distanceButton, 2, 2);

        gradientButton.setOnMouseClicked((event) -> {
            try {
                GradientWatershed wshed = new GradientWatershed(currentFile);
                Stage stage = new Stage();
                Scene scene = new Scene(createGradientGridPane(wshed), 140, 200);
                stage.setResizable(false);
                stage.setTitle("Gradient");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        distanceButton.setOnMouseClicked((event) -> {
            DistanceWatershed wshed = new DistanceWatershed(currentFile);
            Stage stage = new Stage();
            Scene scene = new Scene(createDistanceGridPane(wshed), 400, 200);
            stage.setResizable(false);
            stage.setTitle("Distance");
            stage.setScene(scene);
            stage.show();
        });


        Scene scene = new Scene(gridPane, 400, 130);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    @Override
    public void stop() {
    }

    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

    private GridPane createMainGridPane(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: #35b1b8;");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 0));

        Button fileButton = new Button("Choose a file...");
        fileButton.setPrefSize(200, 20);
        grid.add(fileButton, 1, 0);

        Label label = new Label("File not set");
        label.setPrefSize(100, 20);
        grid.add(label, 2, 0);

        fileButton.setOnAction(value -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("resources"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            String name = selectedFile.getName();
            currentFile = selectedFile.getAbsolutePath();

            label.setText(name);
        });
        return grid;
    }

    public GridPane createGradientGridPane(GradientWatershed wshed) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: #35b1b8;");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 0));

        Label gradientWidthLabel = new Label("Filter width:");
        grid.add(gradientWidthLabel, 1, 0);
        TextField gradientWidthTF = new TextField("3");
        gradientWidthTF.setPrefSize(50, 20);
        grid.add(gradientWidthTF, 2, 0);

        Label gradientHeightLabel = new Label("Filter height:");
        grid.add(gradientHeightLabel, 1, 1);
        TextField gradientHeighthTF = new TextField("3");
        gradientHeighthTF.setPrefSize(50, 20);
        grid.add(gradientHeighthTF, 2, 1);

        Label gradientLabel = new Label("Max gradient:");
        grid.add(gradientLabel, 1, 2);
        TextField gradientTF = new TextField("3");
        gradientTF.setPrefSize(50, 20);
        grid.add(gradientTF, 2, 2);


        Button updateButton = new Button("Adjust Parameters");
        updateButton.setPrefSize(150, 20);
        grid.add(updateButton, 1, 3, 2, 1);

        Button runButton = new Button("Run Watershed");
        runButton.setPrefSize(150, 20);
        grid.add(runButton, 1, 4, 2, 1);

        runButton.setOnAction(value -> {
            try {
                wshed.run();
                exit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        updateButton.setOnAction(value -> {
            wshed.updateParameters(Integer.parseInt(gradientWidthTF.getText()),
                    Integer.parseInt(gradientHeighthTF.getText()),
                    Integer.parseInt(gradientTF.getText()));
        });
        return grid;
    }

    public GridPane createDistanceGridPane(DistanceWatershed wshed) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: #35b1b8;");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 0));

        Label elementWidthLabel = new Label("Element width:");
        grid.add(elementWidthLabel, 1, 0);
        TextField elementWidthTF = new TextField("3");
        setIntegerOnly(elementWidthTF);
        elementWidthTF.setPrefSize(50, 20);
        grid.add(elementWidthTF, 2, 0);

        Label elementHeightLabel = new Label("Element height:");
        grid.add(elementHeightLabel, 1, 1);
        TextField elementHeightTF = new TextField("3");
        setIntegerOnly(elementHeightTF);
        elementHeightTF.setPrefSize(50, 20);
        grid.add(elementHeightTF, 2, 1);

        Button updateButton = new Button("Adjust Parameters");
        updateButton.setPrefSize(150, 20);
        grid.add(updateButton, 1, 3, 2, 1);

        Button runButton = new Button("Run Watershed");
        runButton.setPrefSize(150, 20);
        grid.add(runButton, 1, 4, 2, 1);

        runButton.setOnAction(value -> {
            try {
                wshed.run();
                exit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        updateButton.setOnAction(value -> {
            wshed.updateParameters(Integer.parseInt(elementWidthTF.getText()),
                    Integer.parseInt(elementHeightTF.getText()));
        });

        return grid;
    }

    private void setIntegerOnly(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = delta -> {
            String intText = delta.getText();
            if (intText.matches("[0-9]*")) {
                return delta;
            }
            return null;
        };
        TextFormatter<String> intFormatter = new TextFormatter<>(filter);
        field.setTextFormatter(intFormatter);
    }


}