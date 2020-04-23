package watershed.application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import javafx.application.Application;
import javafx.stage.Stage;
import watershed.algorithm.BaseWatershed;
import watershed.algorithm.DistanceWatershed;
import watershed.algorithm.GradientWatershed;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;


public class MainWindow extends Application {

    private static String currentFile = null;
    FileChooser fileChooser = null;
    Stage primaryStage = null;
    @Override
    public void init() {
        nu.pattern.OpenCV.loadShared();
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
                if (currentFile == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file first");
                    alert.showAndWait();
                } else {
                    try {
                    GradientWatershed wshed = new GradientWatershed(currentFile);
                    Stage stage = new Stage();
                    Scene scene = new Scene(createGradientGridPane(wshed), 200, 200);
                    stage.setResizable(false);
                    stage.setTitle("Gradient");
                    stage.setScene(scene);
                    primaryStage.hide();
                    stage.setOnCloseRequest(closingEvent -> primaryStage.show());
                    stage.showAndWait();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        distanceButton.setOnMouseClicked((event) -> {
            if (currentFile == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Choose file first");
                alert.showAndWait();
            } else {
                DistanceWatershed wshed = new DistanceWatershed(currentFile);
                Stage stage = new Stage();
                Scene scene = new Scene(createDistanceGridPane(wshed), 200, 200);
                stage.setResizable(false);
                stage.setTitle("Distance");
                stage.setScene(scene);
                stage.show();
            }
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
        this.primaryStage = primaryStage;
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
            if (fileChooser == null) {
                fileChooser = new FileChooser();
            }
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                String name = selectedFile.getName();
                currentFile = selectedFile.getAbsolutePath();
                fileChooser.setInitialDirectory(selectedFile.getParentFile());
                label.setText(name);
            }
        });
        return grid;
    }

    public GridPane createGradientGridPane(GradientWatershed wshed) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color: #35b1b8;");
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 0, 25));

        Label gradientWidthLabel = new Label("Filter width:");
        grid.add(gradientWidthLabel, 1, 0);
        TextField gradientWidthTF = new TextField("6");
        gradientWidthTF.setPrefSize(50, 20);
        grid.add(gradientWidthTF, 2, 0);

        Label gradientHeightLabel = new Label("Filter height:");
        grid.add(gradientHeightLabel, 1, 1);
        TextField gradientHeighthTF = new TextField("6");
        gradientHeighthTF.setPrefSize(50, 20);
        grid.add(gradientHeighthTF, 2, 1);

        Label gradientLabel = new Label("Max gradient:");
        grid.add(gradientLabel, 1, 2);
        TextField gradientTF = new TextField("15");
        gradientTF.setPrefSize(50, 20);
        grid.add(gradientTF, 2, 2);

        Label invertSelectionLabel = new Label("Invert selection");
        grid.add(invertSelectionLabel, 1, 3);
        CheckBox invertSelectionCB = new CheckBox();
        grid.add(invertSelectionCB, 2,3);

        Button updateButton = new Button("Adjust Parameters");
        updateButton.setPrefSize(150, 20);
        grid.add(updateButton, 1, 4, 2, 1);

        Button runButton = new Button("Run Watershed");
        runButton.setPrefSize(150, 20);
        grid.add(runButton, 1, 5, 2, 1);

        runButton.setOnAction(value -> {
            try {
                handleRun(runButton, wshed);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        invertSelectionCB.setOnAction( value -> {
            wshed.setInvertSelection(invertSelectionCB.isSelected());
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
        grid.setPadding(new Insets(10, 10, 0, 25));

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

        Label invertSelectionLabel = new Label("Invert selection");
        grid.add(invertSelectionLabel, 1, 2);
        CheckBox invertSelectionCB = new CheckBox();
        grid.add(invertSelectionCB, 2,2);


        Button updateButton = new Button("Adjust Parameters");
        updateButton.setPrefSize(150, 20);
        grid.add(updateButton, 1, 3, 3, 1);

        Button runButton = new Button("Run Watershed");
        runButton.setPrefSize(150, 20);
        grid.add(runButton, 1, 4, 3, 1);

        runButton.setOnAction(value -> {
            try {
                handleRun(runButton, wshed);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        invertSelectionCB.setOnAction( value -> {
            wshed.setInvertSelection(invertSelectionCB.isSelected());
        });

        updateButton.setOnAction(value -> {
            System.out.println(Integer.parseInt((elementWidthTF.getText()) )+" test: " + Integer.parseInt(elementHeightTF.getText()));
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

    private void handleRun (Button runButton, BaseWatershed wshed) throws IOException, URISyntaxException {
        runButton.setDisable(true);
        wshed.run();
        URI uri = MainWindow.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String path = Paths.get(uri).getParent().toString();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Results stored at: " + path);
        alert.showAndWait();
        runButton.setDisable(false);
    }


}